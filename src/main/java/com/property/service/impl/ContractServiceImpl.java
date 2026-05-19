package com.property.service.impl;

import com.property.service.ContractService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Contract;
import com.property.mapper.ContractMapper;
import com.property.dto.request.*;
import com.property.dto.response.ContractVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractMapper contractMapper;

    public PageResult<ContractVO> page(ContractQueryDTO query) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<Contract>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Contract::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getTenantId()), Contract::getTenantId, query.getTenantId())
                .eq(StrUtil.isNotBlank(query.getType()), Contract::getType, query.getType())
                .orderByDesc(Contract::getCreatedAt);
        Page<Contract> raw = contractMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<ContractVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public ContractVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public ContractVO create(ContractDTO dto) {
        if (existsByContractNo(dto.getContractNo())) {
            throw new BusinessException(ErrorCode.CONTRACT_NO_DUPLICATE);
        }
        Contract entity = new Contract();
        applyDTO(entity, dto);
        entity.setContractNo(dto.getContractNo());
        entity.setStatus("DRAFT");
        contractMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public ContractVO update(String id, ContractDTO dto) {
        Contract entity = requireExist(id);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        if (dto.getContractNo() != null && !dto.getContractNo().equals(entity.getContractNo())
                && existsByContractNo(dto.getContractNo())) {
            throw new BusinessException(ErrorCode.CONTRACT_NO_DUPLICATE);
        }
        applyDTO(entity, dto);
        contractMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(String id) {
        Contract entity = requireExist(id);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        contractMapper.deleteById(entity.getId());
    }

    @Transactional
    public ContractVO submit(String id) {
        Contract entity = requireExist(id);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        entity.setStatus("PENDING_APPROVAL");
        contractMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ContractVO approve(String id) {
        Contract entity = requireExist(id);
        if (!"PENDING_APPROVAL".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        entity.setStatus("ACTIVE");
        contractMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ContractVO reject(String id, ContractRejectDTO dto) {
        Contract entity = requireExist(id);
        if (!"PENDING_APPROVAL".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        if (StrUtil.isBlank(dto.getRejectReason())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        entity.setStatus("DRAFT");
        entity.setRejectReason(dto.getRejectReason());
        contractMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ContractVO terminate(String id, ContractTerminateDTO dto) {
        Contract entity = requireExist(id);
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        if (StrUtil.isBlank(dto.getTerminateReason())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        entity.setStatus("TERMINATED");
        entity.setTerminateReason(dto.getTerminateReason());
        contractMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ContractVO renew(String id, ContractRenewDTO dto) {
        Contract entity = requireExist(id);
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.CONTRACT_STATUS_ILLEGAL);
        }
        if (dto.getEndDate() == null || !dto.getEndDate().isAfter(entity.getEndDate())) {
            throw new BusinessException(ErrorCode.CONTRACT_END_DATE_ILLEGAL);
        }
        entity.setEndDate(dto.getEndDate());
        contractMapper.updateById(entity);
        return toVO(entity);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Contract requireExist(String id) {
        Contract entity = contractMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.CONTRACT_NOT_FOUND);
        return entity;
    }

    private boolean existsByContractNo(String contractNo) {
        return contractMapper.exists(
                new LambdaQueryWrapper<Contract>().eq(Contract::getContractNo, contractNo));
    }

    private void applyDTO(Contract entity, ContractDTO dto) {
        if (dto.getContractNo() != null) entity.setContractNo(dto.getContractNo());
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getType() != null) entity.setType(dto.getType());
        if (dto.getTenantId() != null) entity.setTenantId(dto.getTenantId());
        if (dto.getTenantName() != null) entity.setTenantName(dto.getTenantName());
        if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) entity.setEndDate(dto.getEndDate());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
    }

    private ContractVO toVO(Contract e) {
        ContractVO vo = new ContractVO();
        vo.setId(e.getId());
        vo.setContractNo(e.getContractNo());
        vo.setTitle(e.getTitle());
        vo.setType(e.getType());
        vo.setTenantId(e.getTenantId());
        vo.setTenantName(e.getTenantName());
        vo.setStartDate(e.getStartDate());
        vo.setEndDate(e.getEndDate());
        vo.setAmount(e.getAmount());
        vo.setStatus(e.getStatus());
        vo.setRejectReason(e.getRejectReason());
        vo.setTerminateReason(e.getTerminateReason());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
