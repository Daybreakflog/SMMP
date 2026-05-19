package com.property.controller;

import com.property.service.SysDictItemService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.SysDictItemDTO;
import com.property.dto.request.SysDictItemQueryDTO;
import com.property.dto.response.SysDictItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SysDictItem", description = "字典管理")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class SysDictItemController {

    private final SysDictItemService sysDictItemService;

    @Operation(summary = "字典分页列表")
    @GetMapping
    @SaCheckPermission("dict:view")
    public PageResult<SysDictItemVO> list(@ModelAttribute SysDictItemQueryDTO query) {
        return sysDictItemService.page(query);
    }

    @Operation(summary = "按类型查全部条目")
    @GetMapping("/{type}")
    @SaCheckPermission("dict:view")
    public List<SysDictItemVO> listByType(
            @Parameter(description = "字典类型") @PathVariable String type) {
        return sysDictItemService.listByType(type);
    }

    @Operation(summary = "新增字典项")
    @PostMapping
    @SaCheckPermission("dict:edit")
    public ResponseEntity<ApiEnvelope<SysDictItemVO>> create(@Valid @RequestBody SysDictItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.ok(sysDictItemService.create(dto)));
    }

    @Operation(summary = "修改字典项")
    @PutMapping("/{id}")
    @SaCheckPermission("dict:edit")
    public SysDictItemVO update(
            @Parameter(description = "字典项ID") @PathVariable String id,
            @RequestBody SysDictItemDTO dto) {
        return sysDictItemService.update(id, dto);
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/{id}")
    @SaCheckPermission("dict:edit")
    public ResponseEntity<Void> delete(@Parameter(description = "字典项ID") @PathVariable String id) {
        sysDictItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
