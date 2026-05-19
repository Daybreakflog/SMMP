package com.property.controller;

import com.property.service.TemplateService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.TemplateDTO;
import com.property.dto.request.TemplateQueryDTO;
import com.property.dto.response.TemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Template", description = "消息模板管理")
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @Operation(summary = "模板列表（分页）")
    @GetMapping
    @SaCheckPermission("template:view")
    public PageResult<TemplateVO> list(@ModelAttribute TemplateQueryDTO query) {
        return templateService.page(query);
    }

    @Operation(summary = "新建模板", description = "成功返回 HTTP 201")
    @PostMapping
    @SaCheckPermission("template:manage")
    public ResponseEntity<ApiEnvelope<TemplateVO>> create(@Valid @RequestBody TemplateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(templateService.create(dto)));
    }

    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    @SaCheckPermission("template:manage")
    public TemplateVO update(@PathVariable String id, @Valid @RequestBody TemplateDTO dto) {
        return templateService.update(id, dto);
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    @SaCheckPermission("template:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
