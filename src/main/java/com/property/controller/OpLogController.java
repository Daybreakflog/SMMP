package com.property.controller;

import com.property.service.OpLogService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.OpLogQueryDTO;
import com.property.dto.response.OpLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OpLog", description = "操作日志")
@RestController
@RequestMapping("/oplogs")
@RequiredArgsConstructor
public class OpLogController {

    private final OpLogService opLogService;

    @Operation(summary = "操作日志分页列表")
    @GetMapping
    @SaCheckPermission("oplog:view")
    public PageResult<OpLogVO> list(@ModelAttribute OpLogQueryDTO query) {
        return opLogService.page(query);
    }

    @Operation(summary = "操作日志详情")
    @GetMapping("/{id}")
    @SaCheckPermission("oplog:view")
    public OpLogVO get(@Parameter(description = "日志ID") @PathVariable Long id) {
        return opLogService.getById(id);
    }
}
