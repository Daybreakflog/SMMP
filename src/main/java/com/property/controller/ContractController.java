package com.property.controller;

import com.property.service.ContractService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.*;
import com.property.dto.response.ContractVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contract", description = "合同管理")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "合同列表（分页，?status&tenantId&type&page&pageSize）")
    @GetMapping
    public PageResult<ContractVO> list(@ModelAttribute ContractQueryDTO query) {
        return contractService.page(query);
    }

    @Operation(summary = "合同详情")
    @GetMapping("/{id}")
    public ContractVO get(@PathVariable String id) {
        return contractService.getById(id);
    }

    @Operation(summary = "新增合同（草稿状态），返回 201")
    @PostMapping
    @SaCheckPermission("contract:manage")
    public ResponseEntity<ContractVO> create(@RequestBody ContractDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.create(dto));
    }

    @Operation(summary = "编辑合同（仅 DRAFT 可编辑）")
    @PutMapping("/{id}")
    @SaCheckPermission("contract:manage")
    public ContractVO update(@PathVariable String id, @RequestBody ContractDTO dto) {
        return contractService.update(id, dto);
    }

    @Operation(summary = "删除合同（逻辑删除，仅 DRAFT 可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("contract:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "提交审批（DRAFT → PENDING_APPROVAL）")
    @PutMapping("/{id}/submit")
    @SaCheckPermission("contract:manage")
    public ContractVO submit(@PathVariable String id) {
        return contractService.submit(id);
    }

    @Operation(summary = "审批通过（PENDING_APPROVAL → ACTIVE）")
    @PutMapping("/{id}/approve")
    @SaCheckPermission("contract:approve")
    public ContractVO approve(@PathVariable String id) {
        return contractService.approve(id);
    }

    @Operation(summary = "审批驳回（PENDING_APPROVAL → DRAFT，需填驳回原因）")
    @PutMapping("/{id}/reject")
    @SaCheckPermission("contract:approve")
    public ContractVO reject(@PathVariable String id, @RequestBody ContractRejectDTO dto) {
        return contractService.reject(id, dto);
    }

    @Operation(summary = "终止合同（ACTIVE → TERMINATED，需填终止原因）")
    @PutMapping("/{id}/terminate")
    @SaCheckPermission("contract:manage")
    public ContractVO terminate(@PathVariable String id, @RequestBody ContractTerminateDTO dto) {
        return contractService.terminate(id, dto);
    }

    @Operation(summary = "续签合同（ACTIVE 状态，更新到期日期）")
    @PutMapping("/{id}/renew")
    @SaCheckPermission("contract:manage")
    public ContractVO renew(@PathVariable String id, @RequestBody ContractRenewDTO dto) {
        return contractService.renew(id, dto);
    }
}
