package com.property.controller;

import com.property.service.DictService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.dto.request.DictItemDTO;
import com.property.dto.response.DictItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Dict", description = "数据字典")
@RestController
@RequestMapping("/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "字典列表（按 type 分组）")
    @GetMapping
    @SaCheckPermission("dict:view")
    public Map<String, List<DictItemVO>> list() {
        return dictService.listGrouped();
    }

    @Operation(summary = "新建字典项")
    @PostMapping
    @SaCheckPermission("dict:manage")
    public DictItemVO create(@RequestBody DictItemDTO dto) {
        return dictService.create(dto);
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/{id}")
    @SaCheckPermission("dict:manage")
    public DictItemVO update(@PathVariable String id, @RequestBody DictItemDTO dto) {
        return dictService.update(id, dto);
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/{id}")
    @SaCheckPermission("dict:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        dictService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
