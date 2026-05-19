package com.property.controller;

import com.property.service.UploadService;

import com.property.common.api.ApiEnvelope;
import com.property.dto.request.UploadDTO;
import com.property.dto.response.UploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Upload", description = "文件上传记录")
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "记录上传元数据，返回 url")
    @PostMapping
    public ResponseEntity<ApiEnvelope<UploadVO>> create(@RequestBody UploadDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(uploadService.create(dto)));
    }

    @Operation(summary = "查询上传记录")
    @GetMapping("/{id}")
    public UploadVO get(@PathVariable String id) {
        return uploadService.getById(id);
    }
}
