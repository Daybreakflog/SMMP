package com.property.controller;

import com.property.service.TenantService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.TenantDTO;
import com.property.dto.request.TenantQueryDTO;
import com.property.dto.response.ImportPreviewVO;
import com.property.dto.response.TenantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Tenant", description = "租户管理：CRUD + Excel 批量导入")
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "租户列表（分页）")
    @GetMapping
    @SaCheckPermission("tenant:view")
    public PageResult<TenantVO> list(@ModelAttribute TenantQueryDTO query) {
        return tenantService.page(query);
    }

    @Operation(summary = "新增租户", description = "成功返回 HTTP 201")
    @PostMapping
    @SaCheckPermission("tenant:create")
    public ResponseEntity<ApiEnvelope<TenantVO>> create(@Valid @RequestBody TenantDTO dto) {
        TenantVO vo = tenantService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(vo));
    }

    @Operation(summary = "租户详情")
    @GetMapping("/{id}")
    @SaCheckPermission("tenant:view")
    public TenantVO get(@Parameter(description = "租户ID") @PathVariable String id) {
        return tenantService.getById(id);
    }

    @Operation(summary = "编辑租户（部分更新）")
    @PatchMapping("/{id}")
    @SaCheckPermission("tenant:edit")
    public TenantVO update(
            @Parameter(description = "租户ID") @PathVariable String id,
            @RequestBody TenantDTO dto) {
        return tenantService.update(id, dto);
    }

    @Operation(summary = "删除租户（软删）", description = "成功返回 HTTP 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("tenant:delete")
    public ResponseEntity<Void> delete(@Parameter(description = "租户ID") @PathVariable String id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "租户关联合同（占位）")
    @GetMapping("/{id}/contracts")
    @SaCheckPermission("tenant:view")
    public List<Object> contracts(@Parameter(description = "租户ID") @PathVariable String id) {
        return tenantService.getContracts(id);
    }

    @Operation(summary = "租户关联工单（占位）")
    @GetMapping("/{id}/work-orders")
    @SaCheckPermission("tenant:view")
    public List<Object> workOrders(@Parameter(description = "租户ID") @PathVariable String id) {
        return tenantService.getWorkOrders(id);
    }

    @Operation(summary = "Excel 导入预览", description = "解析并校验 Excel，不写库，返回成功/失败行数")
    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("tenant:import")
    public ImportPreviewVO importPreview(
            @Parameter(description = "Excel 文件（.xlsx）") @RequestParam("file") MultipartFile file)
            throws IOException {
        return tenantService.importPreview(file);
    }

    @Operation(summary = "Excel 导入提交", description = "校验通过的行写库，失败行记录原因不中断")
    @PostMapping(value = "/import/commit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("tenant:import")
    public ImportPreviewVO importCommit(
            @Parameter(description = "Excel 文件（.xlsx）") @RequestParam("file") MultipartFile file)
            throws IOException {
        return tenantService.importCommit(file);
    }
}
