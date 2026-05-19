package com.property.service.impl;

import com.property.service.WorkOrderService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.WoAttachment;
import com.property.entity.WoMessage;
import com.property.entity.WoTimeline;
import com.property.entity.WorkOrder;
import com.property.ws.WorkOrderEndpoint;
import com.property.mapper.WoAttachmentMapper;
import com.property.mapper.WoMessageMapper;
import com.property.mapper.WoTimelineMapper;
import com.property.mapper.WorkOrderMapper;
import com.property.dto.request.WorkOrderAssignDTO;
import com.property.dto.request.AttachmentDTO;
import com.property.dto.request.MessageDTO;
import com.property.dto.request.WorkOrderDTO;
import com.property.dto.request.WorkOrderQueryDTO;
import com.property.dto.response.WoAttachmentVO;
import com.property.dto.response.WoMessageVO;
import com.property.dto.response.WorkOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WoTimelineMapper woTimelineMapper;
    private final WoMessageMapper woMessageMapper;
    private final WoAttachmentMapper woAttachmentMapper;
    private final ObjectMapper objectMapper;

    public PageResult<WorkOrderVO> page(WorkOrderQueryDTO query) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .eq(StrUtil.isNotBlank(query.getType()), WorkOrder::getCategory, query.getType())
                .eq(StrUtil.isNotBlank(query.getStatus()), WorkOrder::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getAssigneeId()), WorkOrder::getMaintainerId, query.getAssigneeId())
                .orderByDesc(WorkOrder::getCreatedAt);
        return PageResult.of(workOrderMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    @Transactional
    public WorkOrderVO create(WorkOrderDTO dto) {
        WorkOrder wo = new WorkOrder();
        wo.setNo("WO" + System.currentTimeMillis());
        applyDTO(wo, dto);
        wo.setStatus("PENDING");
        workOrderMapper.insert(wo);
        return toVO(wo);
    }

    public WorkOrderVO getById(String id) {
        WorkOrder wo = requireExist(id);
        return toVO(wo);
    }

    @Transactional
    public WorkOrderVO patch(String id, WorkOrderDTO dto) {
        WorkOrder wo = requireExist(id);
        if (dto.getTitle() != null) wo.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wo.setDescription(dto.getDescription());
        if (dto.getPriority() != null) wo.setPriority(dto.getPriority());
        workOrderMapper.updateById(wo);
        return toVO(wo);
    }

    @Transactional
    public WorkOrderVO assign(String id, WorkOrderAssignDTO dto) {
        WorkOrder wo = requireExist(id);
        String from = wo.getStatus();
        wo.setMaintainerId(dto.getAssigneeId());
        wo.setStatus("ASSIGNED");
        workOrderMapper.updateById(wo);
        addTimeline(wo.getId(), "ASSIGN", from, "ASSIGNED", operatorId());
        return toVO(wo);
    }

    @Transactional
    public WorkOrderVO start(String id) {
        WorkOrder wo = requireExist(id);
        String from = wo.getStatus();
        if (!"PENDING".equals(from) && !"ASSIGNED".equals(from)) {
            throw new BusinessException(ErrorCode.STATUS_ILLEGAL);
        }
        wo.setStatus("IN_PROGRESS");
        workOrderMapper.updateById(wo);
        addTimeline(wo.getId(), "START", from, "IN_PROGRESS", operatorId());
        pushStatusChange(wo);
        return toVO(wo);
    }

    @Transactional
    public WorkOrderVO complete(String id) {
        WorkOrder wo = requireExist(id);
        if (!"IN_PROGRESS".equals(wo.getStatus())) {
            throw new BusinessException(ErrorCode.STATUS_ILLEGAL);
        }
        wo.setStatus("DONE");
        wo.setCompletedAt(LocalDateTime.now());
        workOrderMapper.updateById(wo);
        addTimeline(wo.getId(), "COMPLETE", "IN_PROGRESS", "DONE", operatorId());
        pushStatusChange(wo);
        return toVO(wo);
    }

    @Transactional
    public WorkOrderVO close(String id) {
        WorkOrder wo = requireExist(id);
        String from = wo.getStatus();
        wo.setStatus("CLOSED");
        workOrderMapper.updateById(wo);
        addTimeline(wo.getId(), "CLOSE", from, "CLOSED", operatorId());
        pushStatusChange(wo);
        return toVO(wo);
    }

    @Transactional
    public WorkOrderVO reopen(String id) {
        WorkOrder wo = requireExist(id);
        if (!"CLOSED".equals(wo.getStatus())) {
            throw new BusinessException(ErrorCode.STATUS_ILLEGAL);
        }
        wo.setStatus("PENDING");
        workOrderMapper.updateById(wo);
        addTimeline(wo.getId(), "REOPEN", "CLOSED", "PENDING", operatorId());
        pushStatusChange(wo);
        return toVO(wo);
    }

    @Transactional
    public WoMessageVO addMessage(String id, MessageDTO dto) {
        requireExist(id);
        WoMessage msg = new WoMessage();
        msg.setWorkOrderId(id);
        msg.setSenderId(dto.getSenderId());
        msg.setContent(dto.getContent());
        msg.setType(dto.getType() != null ? dto.getType() : "TEXT");
        woMessageMapper.insert(msg);
        return toMessageVO(msg);
    }

    public List<WoMessageVO> listMessages(String id) {
        requireExist(id);
        return woMessageMapper.selectList(
                new LambdaQueryWrapper<WoMessage>()
                        .eq(WoMessage::getWorkOrderId, id)
                        .orderByAsc(WoMessage::getCreatedAt)
        ).stream().map(this::toMessageVO).toList();
    }

    @Transactional
    public WoAttachmentVO addAttachment(String id, AttachmentDTO dto) {
        requireExist(id);
        WoAttachment att = new WoAttachment();
        att.setWorkOrderId(id);
        att.setName(dto.getName());
        att.setUrl(dto.getUrl());
        att.setSize(dto.getSize());
        woAttachmentMapper.insert(att);
        return toAttachmentVO(att);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private WorkOrder requireExist(String id) {
        WorkOrder wo = workOrderMapper.selectById(id);
        if (wo == null) throw new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND);
        return wo;
    }

    private void addTimeline(String workOrderId, String action, String from, String to, String operatorId) {
        WoTimeline tl = new WoTimeline();
        tl.setWorkOrderId(workOrderId);
        tl.setAction(action);
        tl.setContent(from + " → " + to);
        tl.setOperatorId(operatorId);
        woTimelineMapper.insert(tl);
    }

    private void pushStatusChange(WorkOrder wo) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "STATUS_CHANGE",
                    "orderId", wo.getId(),
                    "status", wo.getStatus(),
                    "at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
            WorkOrderEndpoint.broadcast(wo.getId(), json);
        } catch (Exception e) {
            log.warn("WS broadcast failed for orderId={}: {}", wo.getId(), e.getMessage());
        }
    }

    private String operatorId() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private void applyDTO(WorkOrder wo, WorkOrderDTO dto) {
        if (dto.getTitle() != null) wo.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wo.setDescription(dto.getDescription());
        if (dto.getCategory() != null) wo.setCategory(dto.getCategory());
        if (dto.getPriority() != null) wo.setPriority(dto.getPriority());
        if (dto.getTenantId() != null) wo.setTenantId(dto.getTenantId());
        if (dto.getUnitId() != null) wo.setUnitId(dto.getUnitId());
        if (dto.getProjectId() != null) wo.setProjectId(dto.getProjectId());
        if (dto.getImages() != null) wo.setImages(dto.getImages());
    }

    private WorkOrderVO toVO(WorkOrder wo) {
        WorkOrderVO vo = new WorkOrderVO();
        vo.setId(wo.getId());
        vo.setNo(wo.getNo());
        vo.setCategory(wo.getCategory());
        vo.setTitle(wo.getTitle());
        vo.setDescription(wo.getDescription());
        vo.setStatus(wo.getStatus());
        vo.setPriority(wo.getPriority());
        vo.setTenantId(wo.getTenantId());
        vo.setUnitId(wo.getUnitId());
        vo.setProjectId(wo.getProjectId());
        vo.setMaintainerId(wo.getMaintainerId());
        vo.setSlaDueAt(wo.getSlaDueAt());
        vo.setCompletedAt(wo.getCompletedAt());
        vo.setRating(wo.getRating());
        vo.setRatingText(wo.getRatingText());
        vo.setImages(wo.getImages());
        vo.setCreatedAt(wo.getCreatedAt());
        vo.setUpdatedAt(wo.getUpdatedAt());
        return vo;
    }

    private WoMessageVO toMessageVO(WoMessage m) {
        WoMessageVO vo = new WoMessageVO();
        vo.setId(m.getId());
        vo.setWorkOrderId(m.getWorkOrderId());
        vo.setSenderId(m.getSenderId());
        vo.setContent(m.getContent());
        vo.setType(m.getType());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    private WoAttachmentVO toAttachmentVO(WoAttachment a) {
        WoAttachmentVO vo = new WoAttachmentVO();
        vo.setId(a.getId());
        vo.setWorkOrderId(a.getWorkOrderId());
        vo.setName(a.getName());
        vo.setUrl(a.getUrl());
        vo.setSize(a.getSize());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
