package com.property.controller;

import com.property.service.ConfigService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.ConfigDTO;
import com.property.dto.response.ConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Config", description = "系统参数配置")
@RestController
@RequestMapping("/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "配置列表（分页）")
    @GetMapping
    @SaCheckPermission("config:view")
    public PageResult<ConfigVO> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return configService.page(page, pageSize);
    }

    @Operation(summary = "新建配置")
    @PostMapping
    @SaCheckPermission("config:manage")
    public ConfigVO create(@RequestBody ConfigDTO dto) {
        return configService.create(dto);
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{id}")
    @SaCheckPermission("config:manage")
    public ConfigVO update(@PathVariable String id, @RequestBody ConfigDTO dto) {
        return configService.update(id, dto);
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @SaCheckPermission("config:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        configService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
