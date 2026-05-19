package com.property.service.impl;

import com.property.service.RepairOrderService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.RepairOrder;
import com.property.entity.User;
import com.property.mapper.RepairOrderMapper;
import com.property.mapper.UserMapper;
import com.property.dto.request.*;
import com.property.dto.response.RepairOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final UserMapper userMapper;

    @Transactional
    public RepairOrderVO create(RepairOrderDTO dto) {
        String userId = StpUtil.getLoginIdAsString();
        User user = userMapper.selectById(userId);
        RepairOrder entity = new RepairOrder();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setLocation(dto.getLocation());
        entity.setReporterId(userId);
        entity.setReporterName(user != null ? user.getName() : null);
        entity.setStatus("PENDING");
        repairOrderMapper.insert(entity);
        return toVO(entity);
    }

    public PageResult<RepairOrderVO> page(RepairOrderQueryDTO query) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(StrUtil.isNotBlank(query.getStatus()), RepairOrder::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getReporterId()), RepairOrder::getReporterId, query.getReporterId())
                .orderByDesc(RepairOrder::getCreatedAt);
        Page<RepairOrder> raw = repairOrderMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<RepairOrderVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public RepairOrderVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public RepairOrderVO update(String id, RepairOrderDTO dto) {
        RepairOrder entity = requireExist(id);
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getLocation() != null) entity.setLocation(dto.getLocation());
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(String id) {
        RepairOrder entity = requireExist(id);
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        repairOrderMapper.deleteById(id);
    }

    @Transactional
    public RepairOrderVO assign(String id, RepairAssignDTO dto) {
        RepairOrder entity = requireExist(id);
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setAssigneeName(dto.getAssigneeName());
        entity.setStatus("ASSIGNED");
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public RepairOrderVO start(String id) {
        RepairOrder entity = requireExist(id);
        if (!"ASSIGNED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        entity.setStatus("IN_PROGRESS");
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public RepairOrderVO complete(String id, RepairCompleteDTO dto) {
        RepairOrder entity = requireExist(id);
        if (!"IN_PROGRESS".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        entity.setResult(dto.getResult());
        entity.setStatus("COMPLETED");
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public RepairOrderVO confirm(String id) {
        RepairOrder entity = requireExist(id);
        if (!"COMPLETED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        entity.setStatus("CONFIRMED");
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public RepairOrderVO reject(String id, RepairRejectDTO dto) {
        RepairOrder entity = requireExist(id);
        if (!"COMPLETED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        entity.setRejectReason(dto.getRejectReason());
        entity.setStatus("IN_PROGRESS");
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public RepairOrderVO cancel(String id) {
        RepairOrder entity = requireExist(id);
        if (!"PENDING".equals(entity.getStatus()) && !"ASSIGNED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        entity.setStatus("CANCELLED");
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public RepairOrderVO rate(String id, RateDTO dto) {
        RepairOrder entity = requireExist(id);
        if (!"CONFIRMED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.REPAIR_STATUS_ILLEGAL);
        }
        if (entity.getRating() != null) {
            throw new BusinessException(ErrorCode.REPAIR_ALREADY_RATED);
        }
        entity.setRating(dto.getRating());
        entity.setRatingComment(dto.getRatingComment());
        repairOrderMapper.updateById(entity);
        return toVO(entity);
    }

    // ── private helpers ─────────────────────────────────────────────

    private RepairOrder requireExist(String id) {
        RepairOrder entity = repairOrderMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        return entity;
    }

    private RepairOrderVO toVO(RepairOrder e) {
        RepairOrderVO vo = new RepairOrderVO();
        vo.setId(e.getId());
        vo.setTitle(e.getTitle());
        vo.setDescription(e.getDescription());
        vo.setLocation(e.getLocation());
        vo.setReporterId(e.getReporterId());
        vo.setReporterName(e.getReporterName());
        vo.setAssigneeId(e.getAssigneeId());
        vo.setAssigneeName(e.getAssigneeName());
        vo.setStatus(e.getStatus());
        vo.setResult(e.getResult());
        vo.setRejectReason(e.getRejectReason());
        vo.setRating(e.getRating());
        vo.setRatingComment(e.getRatingComment());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
