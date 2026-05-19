package com.property.controller;

import com.property.service.ComplaintService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.AppealDTO;
import com.property.dto.request.ComplaintDTO;
import com.property.dto.request.ComplaintQueryDTO;
import com.property.dto.response.ComplaintVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Complaint", description = "投诉管理")
@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @Operation(summary = "投诉列表（分页）")
    @GetMapping
    @SaCheckPermission("complaint:view")
    public PageResult<ComplaintVO> list(@ModelAttribute ComplaintQueryDTO query) {
        return complaintService.page(query);
    }

    @Operation(summary = "提交投诉（小程序，无需鉴权）", description = "成功返回 HTTP 201")
    @PostMapping
    public ResponseEntity<ApiEnvelope<ComplaintVO>> create(@Valid @RequestBody ComplaintDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(complaintService.create(dto)));
    }

    @Operation(summary = "投诉详情")
    @GetMapping("/{id}")
    @SaCheckPermission("complaint:view")
    public ComplaintVO get(@Parameter(description = "投诉ID") @PathVariable String id) {
        return complaintService.getById(id);
    }

    @Operation(summary = "更新投诉（标题/内容/category）")
    @PatchMapping("/{id}")
    @SaCheckPermission("complaint:edit")
    public ComplaintVO patch(
            @Parameter(description = "投诉ID") @PathVariable String id,
            @RequestBody ComplaintDTO dto) {
        return complaintService.patch(id, dto);
    }

    @Operation(summary = "受理（PENDING → HANDLING）")
    @PostMapping("/{id}/accept")
    @SaCheckPermission("complaint:handle")
    public ComplaintVO accept(@Parameter(description = "投诉ID") @PathVariable String id) {
        return complaintService.accept(id);
    }

    @Operation(summary = "解决（HANDLING → RESOLVED）")
    @PostMapping("/{id}/resolve")
    @SaCheckPermission("complaint:handle")
    public ComplaintVO resolve(@Parameter(description = "投诉ID") @PathVariable String id) {
        return complaintService.resolve(id);
    }

    @Operation(summary = "关闭投诉")
    @PostMapping("/{id}/close")
    @SaCheckPermission("complaint:handle")
    public ComplaintVO close(@Parameter(description = "投诉ID") @PathVariable String id) {
        return complaintService.close(id);
    }

    @Operation(summary = "提起申诉（小程序，无需鉴权）")
    @PostMapping("/{id}/appeal")
    public ResponseEntity<Void> appeal(
            @Parameter(description = "投诉ID") @PathVariable String id,
            @Valid @RequestBody AppealDTO dto) {
        complaintService.appeal(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
