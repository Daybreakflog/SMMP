package com.property.controller;

import com.property.service.WorkOrderService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.WorkOrderAssignDTO;
import com.property.dto.request.AttachmentDTO;
import com.property.dto.request.MessageDTO;
import com.property.dto.request.WorkOrderDTO;
import com.property.dto.request.WorkOrderQueryDTO;
import com.property.dto.response.WoAttachmentVO;
import com.property.dto.response.WoMessageVO;
import com.property.dto.response.WorkOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "WorkOrder", description = "工单管理")
@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @Operation(summary = "工单列表（分页）")
    @GetMapping
    @SaCheckPermission("workorder:view")
    public PageResult<WorkOrderVO> list(@ModelAttribute WorkOrderQueryDTO query) {
        return workOrderService.page(query);
    }

    @Operation(summary = "新建工单", description = "成功返回 HTTP 201")
    @PostMapping
    @SaCheckPermission("workorder:create")
    public ResponseEntity<ApiEnvelope<WorkOrderVO>> create(@Valid @RequestBody WorkOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(workOrderService.create(dto)));
    }

    @Operation(summary = "工单详情")
    @GetMapping("/{id}")
    @SaCheckPermission("workorder:view")
    public WorkOrderVO get(@Parameter(description = "工单ID") @PathVariable String id) {
        return workOrderService.getById(id);
    }

    @Operation(summary = "部分更新工单（标题/描述/优先级）")
    @PatchMapping("/{id}")
    @SaCheckPermission("workorder:edit")
    public WorkOrderVO patch(
            @Parameter(description = "工单ID") @PathVariable String id,
            @RequestBody WorkOrderDTO dto) {
        return workOrderService.patch(id, dto);
    }

    @Operation(summary = "派单")
    @PostMapping("/{id}/assign")
    @SaCheckPermission("workorder:assign")
    public WorkOrderVO assign(
            @Parameter(description = "工单ID") @PathVariable String id,
            @Valid @RequestBody WorkOrderAssignDTO dto) {
        return workOrderService.assign(id, dto);
    }

    @Operation(summary = "受理（PENDING/ASSIGNED → IN_PROGRESS）")
    @PostMapping("/{id}/start")
    @SaCheckPermission("workorder:edit")
    public WorkOrderVO start(@Parameter(description = "工单ID") @PathVariable String id) {
        return workOrderService.start(id);
    }

    @Operation(summary = "完成（IN_PROGRESS → DONE）")
    @PostMapping("/{id}/complete")
    @SaCheckPermission("workorder:edit")
    public WorkOrderVO complete(@Parameter(description = "工单ID") @PathVariable String id) {
        return workOrderService.complete(id);
    }

    @Operation(summary = "关闭工单")
    @PostMapping("/{id}/close")
    @SaCheckPermission("workorder:edit")
    public WorkOrderVO close(@Parameter(description = "工单ID") @PathVariable String id) {
        return workOrderService.close(id);
    }

    @Operation(summary = "重开工单（CLOSED → PENDING）")
    @PostMapping("/{id}/reopen")
    @SaCheckPermission("workorder:edit")
    public WorkOrderVO reopen(@Parameter(description = "工单ID") @PathVariable String id) {
        return workOrderService.reopen(id);
    }

    @Operation(summary = "发送消息（小程序，无需鉴权）")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiEnvelope<WoMessageVO>> addMessage(
            @Parameter(description = "工单ID") @PathVariable String id,
            @Valid @RequestBody MessageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(workOrderService.addMessage(id, dto)));
    }

    @Operation(summary = "消息历史")
    @GetMapping("/{id}/messages")
    @SaCheckPermission("workorder:view")
    public List<WoMessageVO> listMessages(@Parameter(description = "工单ID") @PathVariable String id) {
        return workOrderService.listMessages(id);
    }

    @Operation(summary = "上传附件（小程序，无需鉴权）")
    @PostMapping("/{id}/attachments")
    public ResponseEntity<ApiEnvelope<WoAttachmentVO>> addAttachment(
            @Parameter(description = "工单ID") @PathVariable String id,
            @Valid @RequestBody AttachmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(workOrderService.addAttachment(id, dto)));
    }
}
