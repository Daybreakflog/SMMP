package com.property.service.impl;

import com.property.service.ComplaintService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Appeal;
import com.property.entity.Complaint;
import com.property.entity.ComplaintTimeline;
import com.property.mapper.AppealMapper;
import com.property.mapper.ComplaintMapper;
import com.property.mapper.ComplaintTimelineMapper;
import com.property.dto.request.AppealDTO;
import com.property.dto.request.ComplaintDTO;
import com.property.dto.request.ComplaintQueryDTO;
import com.property.dto.response.ComplaintVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final ComplaintTimelineMapper complaintTimelineMapper;
    private final AppealMapper appealMapper;

    public PageResult<ComplaintVO> page(ComplaintQueryDTO query) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<Complaint>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Complaint::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getTenantId()), Complaint::getTenantId, query.getTenantId())
                .orderByDesc(Complaint::getCreatedAt);
        return PageResult.of(complaintMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    @Transactional
    public ComplaintVO create(ComplaintDTO dto) {
        Complaint c = new Complaint();
        c.setNo("CP" + System.currentTimeMillis());
        applyDTO(c, dto);
        c.setStatus("PENDING");
        complaintMapper.insert(c);
        return toVO(c);
    }

    public ComplaintVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public ComplaintVO patch(String id, ComplaintDTO dto) {
        Complaint c = requireExist(id);
        if (dto.getTitle() != null) c.setTitle(dto.getTitle());
        if (dto.getContent() != null) c.setContent(dto.getContent());
        if (dto.getCategory() != null) c.setCategory(dto.getCategory());
        complaintMapper.updateById(c);
        return toVO(c);
    }

    @Transactional
    public ComplaintVO accept(String id) {
        Complaint c = requireExist(id);
        if (!"PENDING".equals(c.getStatus())) {
            throw new BusinessException(ErrorCode.STATUS_ILLEGAL);
        }
        c.setStatus("HANDLING");
        complaintMapper.updateById(c);
        addTimeline(c.getId(), "ACCEPT", "PENDING", "HANDLING");
        return toVO(c);
    }

    @Transactional
    public ComplaintVO resolve(String id) {
        Complaint c = requireExist(id);
        if (!"HANDLING".equals(c.getStatus())) {
            throw new BusinessException(ErrorCode.STATUS_ILLEGAL);
        }
        c.setStatus("RESOLVED");
        c.setResolvedAt(LocalDateTime.now());
        complaintMapper.updateById(c);
        addTimeline(c.getId(), "RESOLVE", "HANDLING", "RESOLVED");
        return toVO(c);
    }

    @Transactional
    public ComplaintVO close(String id) {
        Complaint c = requireExist(id);
        String from = c.getStatus();
        c.setStatus("CLOSED");
        complaintMapper.updateById(c);
        addTimeline(c.getId(), "CLOSE", from, "CLOSED");
        return toVO(c);
    }

    @Transactional
    public void appeal(String id, AppealDTO dto) {
        Complaint c = requireExist(id);
        Appeal appeal = new Appeal();
        appeal.setComplaintId(id);
        appeal.setReason(dto.getReason());
        appeal.setAppealedAt(LocalDateTime.now());
        appealMapper.insert(appeal);

        // reset complaint for re-handling
        String from = c.getStatus();
        c.setStatus("HANDLING");
        complaintMapper.updateById(c);
        addTimeline(c.getId(), "APPEAL", from, "HANDLING");
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Complaint requireExist(String id) {
        Complaint c = complaintMapper.selectById(id);
        if (c == null) throw new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND);
        return c;
    }

    private void addTimeline(String complaintId, String action, String from, String to) {
        String operatorId = null;
        try {
            operatorId = StpUtil.getLoginIdAsString();
        } catch (Exception ignored) {
        }
        ComplaintTimeline tl = new ComplaintTimeline();
        tl.setComplaintId(complaintId);
        tl.setAction(action);
        tl.setContent(from + " → " + to);
        tl.setOperatorId(operatorId);
        complaintTimelineMapper.insert(tl);
    }

    private void applyDTO(Complaint c, ComplaintDTO dto) {
        if (dto.getTitle() != null) c.setTitle(dto.getTitle());
        if (dto.getContent() != null) c.setContent(dto.getContent());
        if (dto.getCategory() != null) c.setCategory(dto.getCategory());
        if (dto.getTenantId() != null) c.setTenantId(dto.getTenantId());
        if (dto.getUnitId() != null) c.setUnitId(dto.getUnitId());
        if (dto.getProjectId() != null) c.setProjectId(dto.getProjectId());
    }

    private ComplaintVO toVO(Complaint c) {
        ComplaintVO vo = new ComplaintVO();
        vo.setId(c.getId());
        vo.setNo(c.getNo());
        vo.setTitle(c.getTitle());
        vo.setContent(c.getContent());
        vo.setCategory(c.getCategory());
        vo.setStatus(c.getStatus());
        vo.setTenantId(c.getTenantId());
        vo.setUnitId(c.getUnitId());
        vo.setProjectId(c.getProjectId());
        vo.setHandlerId(c.getHandlerId());
        vo.setResolvedAt(c.getResolvedAt());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }
}
