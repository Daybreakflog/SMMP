package com.property.controller;

import com.property.service.FeeItemService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.ApiEnvelope;
import com.property.dto.request.FeeItemDTO;
import com.property.dto.response.FeeItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bill", description = "账单管理：收费项目 + 账单 + 收款")
@RestController
@RequestMapping("/fee-items")
@RequiredArgsConstructor
public class FeeItemController {

    private final FeeItemService feeItemService;

    @Operation(summary = "收费项目列表")
    @GetMapping
    @SaCheckPermission("bill:view")
    public List<FeeItemVO> list() {
        return feeItemService.list();
    }

    @Operation(summary = "新增收费项目", description = "成功返回 HTTP 201")
    @PostMapping
    @SaCheckPermission("bill:edit")
    public ResponseEntity<ApiEnvelope<FeeItemVO>> create(@Valid @RequestBody FeeItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(feeItemService.create(dto)));
    }

    @Operation(summary = "部分更新收费项目")
    @PatchMapping("/{id}")
    @SaCheckPermission("bill:edit")
    public FeeItemVO patch(@PathVariable String id, @RequestBody FeeItemDTO dto) {
        return feeItemService.patch(id, dto);
    }

    @Operation(summary = "删除收费项目（软删）", description = "成功返回 HTTP 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("bill:edit")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        feeItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
