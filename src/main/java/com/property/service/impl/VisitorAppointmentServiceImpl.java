package com.property.service.impl;

import com.property.service.VisitorAppointmentService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.VisitorAppointment;
import com.property.mapper.VisitorAppointmentMapper;
import com.property.dto.request.VisitorAppointmentDTO;
import com.property.dto.request.VisitorAppointmentQueryDTO;
import com.property.dto.response.VisitorAppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorAppointmentServiceImpl implements VisitorAppointmentService {

    private final VisitorAppointmentMapper visitorMapper;

    public PageResult<VisitorAppointmentVO> page(VisitorAppointmentQueryDTO query) {
        LambdaQueryWrapper<VisitorAppointment> wrapper = new LambdaQueryWrapper<VisitorAppointment>()
                .eq(StrUtil.isNotBlank(query.getStatus()), VisitorAppointment::getStatus, query.getStatus())
                .orderByDesc(VisitorAppointment::getCreatedAt);
        Page<VisitorAppointment> raw = visitorMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<VisitorAppointmentVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public VisitorAppointmentVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public VisitorAppointmentVO create(VisitorAppointmentDTO dto) {
        VisitorAppointment entity = new VisitorAppointment();
        applyDTO(entity, dto);
        entity.setApplicantId(StpUtil.getLoginIdAsString());
        entity.setStatus("PENDING");
        visitorMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public VisitorAppointmentVO approve(String id) {
        VisitorAppointment entity = requireExist(id);
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.VISITOR_STATUS_ILLEGAL);
        }
        entity.setStatus("APPROVED");
        entity.setApprovedBy(StpUtil.getLoginIdAsString());
        entity.setApprovedAt(LocalDateTime.now());
        visitorMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public VisitorAppointmentVO reject(String id) {
        VisitorAppointment entity = requireExist(id);
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.VISITOR_STATUS_ILLEGAL);
        }
        entity.setStatus("REJECTED");
        entity.setApprovedBy(StpUtil.getLoginIdAsString());
        entity.setApprovedAt(LocalDateTime.now());
        visitorMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public VisitorAppointmentVO checkIn(String id) {
        VisitorAppointment entity = requireExist(id);
        if (!"APPROVED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.VISITOR_STATUS_ILLEGAL);
        }
        entity.setStatus("CHECKED_IN");
        entity.setCheckInAt(LocalDateTime.now());
        visitorMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public VisitorAppointmentVO checkOut(String id) {
        VisitorAppointment entity = requireExist(id);
        if (!"CHECKED_IN".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.VISITOR_STATUS_ILLEGAL);
        }
        entity.setStatus("CHECKED_OUT");
        entity.setCheckOutAt(LocalDateTime.now());
        visitorMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void cancel(String id) {
        VisitorAppointment entity = requireExist(id);
        String currentUserId = StpUtil.getLoginIdAsString();
        if (!currentUserId.equals(entity.getApplicantId())) {
            throw new BusinessException(ErrorCode.VISITOR_NOT_OWNER);
        }
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.VISITOR_STATUS_ILLEGAL);
        }
        visitorMapper.deleteById(entity.getId());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private VisitorAppointment requireExist(String id) {
        VisitorAppointment entity = visitorMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.VISITOR_NOT_FOUND);
        return entity;
    }

    private void applyDTO(VisitorAppointment entity, VisitorAppointmentDTO dto) {
        if (dto.getVisitorName() != null) entity.setVisitorName(dto.getVisitorName());
        if (dto.getVisitorPhone() != null) entity.setVisitorPhone(dto.getVisitorPhone());
        if (dto.getVisitorIdCard() != null) entity.setVisitorIdCard(dto.getVisitorIdCard());
        if (dto.getPurpose() != null) entity.setPurpose(dto.getPurpose());
        if (dto.getVisitDate() != null) entity.setVisitDate(dto.getVisitDate());
        if (dto.getExpectedArrivalAt() != null) entity.setExpectedArrivalAt(dto.getExpectedArrivalAt());
        if (dto.getExpectedDepartureAt() != null) entity.setExpectedDepartureAt(dto.getExpectedDepartureAt());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
    }

    private VisitorAppointmentVO toVO(VisitorAppointment e) {
        VisitorAppointmentVO vo = new VisitorAppointmentVO();
        vo.setId(e.getId());
        vo.setVisitorName(e.getVisitorName());
        vo.setVisitorPhone(e.getVisitorPhone());
        vo.setVisitorIdCard(e.getVisitorIdCard());
        vo.setPurpose(e.getPurpose());
        vo.setVisitDate(e.getVisitDate());
        vo.setExpectedArrivalAt(e.getExpectedArrivalAt());
        vo.setExpectedDepartureAt(e.getExpectedDepartureAt());
        vo.setStatus(e.getStatus());
        vo.setApplicantId(e.getApplicantId());
        vo.setApprovedBy(e.getApprovedBy());
        vo.setApprovedAt(e.getApprovedAt());
        vo.setCheckInAt(e.getCheckInAt());
        vo.setCheckOutAt(e.getCheckOutAt());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
