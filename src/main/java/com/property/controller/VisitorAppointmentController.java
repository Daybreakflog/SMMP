package com.property.controller;

import com.property.service.VisitorAppointmentService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.VisitorAppointmentDTO;
import com.property.dto.request.VisitorAppointmentQueryDTO;
import com.property.dto.response.VisitorAppointmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Visitor", description = "访客预约管理")
@RestController
@RequestMapping("/visitors")
@RequiredArgsConstructor
public class VisitorAppointmentController {

    private final VisitorAppointmentService visitorService;

    @Operation(summary = "创建访客预约，返回 201")
    @PostMapping
    public ResponseEntity<VisitorAppointmentVO> create(@Valid @RequestBody VisitorAppointmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitorService.create(dto));
    }

    @Operation(summary = "预约列表（分页，?status&page&pageSize）")
    @GetMapping
    public PageResult<VisitorAppointmentVO> list(@ModelAttribute VisitorAppointmentQueryDTO query) {
        return visitorService.page(query);
    }

    @Operation(summary = "预约详情")
    @GetMapping("/{id}")
    public VisitorAppointmentVO get(@PathVariable String id) {
        return visitorService.getById(id);
    }

    @Operation(summary = "审批通过（PENDING → APPROVED）")
    @PutMapping("/{id}/approve")
    @SaCheckPermission("visitor:manage")
    public VisitorAppointmentVO approve(@PathVariable String id) {
        return visitorService.approve(id);
    }

    @Operation(summary = "审批拒绝（PENDING → REJECTED）")
    @PutMapping("/{id}/reject")
    @SaCheckPermission("visitor:manage")
    public VisitorAppointmentVO reject(@PathVariable String id) {
        return visitorService.reject(id);
    }

    @Operation(summary = "签到（APPROVED → CHECKED_IN）")
    @PutMapping("/{id}/check-in")
    @SaCheckPermission("visitor:manage")
    public VisitorAppointmentVO checkIn(@PathVariable String id) {
        return visitorService.checkIn(id);
    }

    @Operation(summary = "签退（CHECKED_IN → CHECKED_OUT）")
    @PutMapping("/{id}/check-out")
    @SaCheckPermission("visitor:manage")
    public VisitorAppointmentVO checkOut(@PathVariable String id) {
        return visitorService.checkOut(id);
    }

    @Operation(summary = "取消预约（逻辑删除，仅本人且 PENDING），返回 204")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        visitorService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
