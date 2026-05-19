package com.property.controller;

import com.property.service.ExportService;

import com.property.common.api.ApiEnvelope;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.dto.request.ExportRequestDTO;
import com.property.dto.response.ExportTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Export", description = "异步导出任务")
@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "创建导出任务（异步），返回 202 + taskId")
    @PostMapping
    public ResponseEntity<ApiEnvelope<Map<String, String>>> create(@Valid @RequestBody ExportRequestDTO dto) {
        ExportTaskVO vo = exportService.create(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiEnvelope.ok(Map.of("taskId", vo.getId())));
    }

    @Operation(summary = "查询导出任务状态")
    @GetMapping("/{id}")
    public ExportTaskVO getStatus(@Parameter(description = "任务ID") @PathVariable String id) {
        return exportService.getById(id);
    }

    @Operation(summary = "获取下载 URL（任务未完成返回 409）")
    @GetMapping("/{id}/download")
    public ResponseEntity<ApiEnvelope<?>> download(@Parameter(description = "任务ID") @PathVariable String id) {
        try {
            String url = exportService.getDownloadUrl(id);
            return ResponseEntity.ok(ApiEnvelope.ok(Map.of("url", url)));
        } catch (BusinessException e) {
            if (e.getCode() == ErrorCode.EXPORT_NOT_READY.getCode()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiEnvelope.fail(e.getCode(), e.getMessage()));
            }
            throw e;
        }
    }
}
