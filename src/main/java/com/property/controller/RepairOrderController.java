package com.property.controller;

import com.property.service.RepairOrderService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.*;
import com.property.dto.response.RepairOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "RepairOrder", description = "报修管理")
@RestController
@RequestMapping("/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @Operation(summary = "住户提交报修申请，返回 201")
    @PostMapping
    @SaCheckPermission("repair:submit")
    public ResponseEntity<RepairOrderVO> create(@Valid @RequestBody RepairOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repairOrderService.create(dto));
    }

    @Operation(summary = "报修列表（分页，?status&reporterId&page&pageSize）")
    @GetMapping
    public PageResult<RepairOrderVO> list(@ModelAttribute RepairOrderQueryDTO query) {
        return repairOrderService.page(query);
    }

    @Operation(summary = "报修详情")
    @GetMapping("/{id}")
    public RepairOrderVO get(@PathVariable String id) {
        return repairOrderService.getById(id);
    }

    @Operation(summary = "编辑报修信息")
    @PutMapping("/{id}")
    @SaCheckPermission("repair:manage")
    public RepairOrderVO update(@PathVariable String id, @Valid @RequestBody RepairOrderDTO dto) {
        return repairOrderService.update(id, dto);
    }

    @Operation(summary = "删除报修（逻辑删除，仅 PENDING 可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("repair:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repairOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "派工（PENDING → ASSIGNED，指定维修人员）")
    @PutMapping("/{id}/assign")
    @SaCheckPermission("repair:manage")
    public RepairOrderVO assign(@PathVariable String id, @Valid @RequestBody RepairAssignDTO dto) {
        return repairOrderService.assign(id, dto);
    }

    @Operation(summary = "开始维修（ASSIGNED → IN_PROGRESS）")
    @PutMapping("/{id}/start")
    @SaCheckPermission("repair:execute")
    public RepairOrderVO start(@PathVariable String id) {
        return repairOrderService.start(id);
    }

    @Operation(summary = "完成维修（IN_PROGRESS → COMPLETED，提交维修结果）")
    @PutMapping("/{id}/complete")
    @SaCheckPermission("repair:execute")
    public RepairOrderVO complete(@PathVariable String id, @Valid @RequestBody RepairCompleteDTO dto) {
        return repairOrderService.complete(id, dto);
    }

    @Operation(summary = "住户确认（COMPLETED → CONFIRMED）")
    @PutMapping("/{id}/confirm")
    @SaCheckPermission("repair:submit")
    public RepairOrderVO confirm(@PathVariable String id) {
        return repairOrderService.confirm(id);
    }

    @Operation(summary = "住户驳回（COMPLETED → IN_PROGRESS，需填写驳回原因）")
    @PutMapping("/{id}/reject")
    @SaCheckPermission("repair:submit")
    public RepairOrderVO reject(@PathVariable String id, @Valid @RequestBody RepairRejectDTO dto) {
        return repairOrderService.reject(id, dto);
    }

    @Operation(summary = "取消报修（PENDING/ASSIGNED → CANCELLED）")
    @PutMapping("/{id}/cancel")
    @SaCheckPermission("repair:submit")
    public RepairOrderVO cancel(@PathVariable String id) {
        return repairOrderService.cancel(id);
    }

    @Operation(summary = "评价（CONFIRMED 后可评价，1~5 分 + 评价内容）")
    @PutMapping("/{id}/rate")
    @SaCheckPermission("repair:submit")
    public RepairOrderVO rate(@PathVariable String id, @Valid @RequestBody RateDTO dto) {
        return repairOrderService.rate(id, dto);
    }
}
