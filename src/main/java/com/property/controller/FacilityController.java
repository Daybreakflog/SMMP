package com.property.controller;

import com.property.service.FacilityService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.FacilityDTO;
import com.property.dto.request.FacilityMaintenanceRecordDTO;
import com.property.dto.request.FacilityQueryDTO;
import com.property.dto.request.MaintenanceRecordQueryDTO;
import com.property.dto.response.FacilityMaintenanceRecordVO;
import com.property.dto.response.FacilityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Facility", description = "设备设施管理")
@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "新增设备，返回 201")
    @PostMapping
    @SaCheckPermission("facility:manage")
    public ResponseEntity<FacilityVO> create(@Valid @RequestBody FacilityDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.create(dto));
    }

    @Operation(summary = "设备列表（分页，?status&category&page&pageSize）")
    @GetMapping
    public PageResult<FacilityVO> list(@ModelAttribute FacilityQueryDTO query) {
        return facilityService.page(query);
    }

    @Operation(summary = "设备详情")
    @GetMapping("/{id}")
    public FacilityVO get(@PathVariable String id) {
        return facilityService.getById(id);
    }

    @Operation(summary = "编辑设备信息")
    @PutMapping("/{id}")
    @SaCheckPermission("facility:manage")
    public FacilityVO update(@PathVariable String id, @Valid @RequestBody FacilityDTO dto) {
        return facilityService.update(id, dto);
    }

    @Operation(summary = "删除设备（逻辑删除，仅 NORMAL 可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("facility:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        facilityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "标记为维护中（NORMAL → MAINTENANCE）")
    @PutMapping("/{id}/maintenance")
    @SaCheckPermission("facility:manage")
    public FacilityVO maintenance(@PathVariable String id) {
        return facilityService.maintenance(id);
    }

    @Operation(summary = "恢复正常（MAINTENANCE → NORMAL）")
    @PutMapping("/{id}/restore")
    @SaCheckPermission("facility:manage")
    public FacilityVO restore(@PathVariable String id) {
        return facilityService.restore(id);
    }

    @Operation(summary = "报废（NORMAL/MAINTENANCE → SCRAPPED，不可逆）")
    @PutMapping("/{id}/scrap")
    @SaCheckPermission("facility:manage")
    public FacilityVO scrap(@PathVariable String id) {
        return facilityService.scrap(id);
    }

    @Operation(summary = "添加维护记录，返回 201")
    @PostMapping("/{id}/maintenance-records")
    @SaCheckPermission("facility:manage")
    public ResponseEntity<FacilityMaintenanceRecordVO> addRecord(
            @PathVariable String id,
            @Valid @RequestBody FacilityMaintenanceRecordDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.addRecord(id, dto));
    }

    @Operation(summary = "维护记录列表（分页）")
    @GetMapping("/{id}/maintenance-records")
    public PageResult<FacilityMaintenanceRecordVO> records(
            @PathVariable String id,
            @ModelAttribute MaintenanceRecordQueryDTO query) {
        return facilityService.recordPage(id, query);
    }
}
