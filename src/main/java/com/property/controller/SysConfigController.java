package com.property.controller;

import com.property.service.SysConfigService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.SysConfigDTO;
import com.property.dto.request.SysConfigQueryDTO;
import com.property.dto.response.SysConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SysConfig", description = "系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "配置分页列表")
    @GetMapping
    @SaCheckPermission("config:view")
    public PageResult<SysConfigVO> list(@ModelAttribute SysConfigQueryDTO query) {
        return sysConfigService.page(query);
    }

    @Operation(summary = "按 key 查单条配置")
    @GetMapping("/{key}")
    @SaCheckPermission("config:view")
    public SysConfigVO get(@Parameter(description = "配置键") @PathVariable String key) {
        return sysConfigService.getByKey(key);
    }

    @Operation(summary = "新增配置")
    @PostMapping
    @SaCheckPermission("config:edit")
    public ResponseEntity<ApiEnvelope<SysConfigVO>> create(@Valid @RequestBody SysConfigDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.ok(sysConfigService.create(dto)));
    }

    @Operation(summary = "修改配置值/描述")
    @PutMapping("/{key}")
    @SaCheckPermission("config:edit")
    public SysConfigVO update(
            @Parameter(description = "配置键") @PathVariable String key,
            @RequestBody SysConfigDTO dto) {
        return sysConfigService.update(key, dto);
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{key}")
    @SaCheckPermission("config:edit")
    public ResponseEntity<Void> delete(@Parameter(description = "配置键") @PathVariable String key) {
        sysConfigService.delete(key);
        return ResponseEntity.noContent().build();
    }
}
