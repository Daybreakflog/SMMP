package com.property.controller;

import com.property.service.ParkingSpaceService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.ParkingSpaceAssignDTO;
import com.property.dto.request.ParkingSpaceDTO;
import com.property.dto.request.ParkingSpaceQueryDTO;
import com.property.dto.response.ParkingSpaceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parking", description = "车位管理")
@RestController
@RequestMapping("/parking-spaces")
@RequiredArgsConstructor
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    @Operation(summary = "新增车位，返回 201")
    @PostMapping
    @SaCheckPermission("parking:manage")
    public ResponseEntity<ParkingSpaceVO> create(@Valid @RequestBody ParkingSpaceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpaceService.create(dto));
    }

    @Operation(summary = "车位列表（分页，?status&zone&page&pageSize）")
    @GetMapping
    public PageResult<ParkingSpaceVO> list(@ModelAttribute ParkingSpaceQueryDTO query) {
        return parkingSpaceService.page(query);
    }

    @Operation(summary = "车位详情")
    @GetMapping("/{id}")
    public ParkingSpaceVO get(@PathVariable String id) {
        return parkingSpaceService.getById(id);
    }

    @Operation(summary = "编辑车位信息")
    @PutMapping("/{id}")
    @SaCheckPermission("parking:manage")
    public ParkingSpaceVO update(@PathVariable String id, @Valid @RequestBody ParkingSpaceDTO dto) {
        return parkingSpaceService.update(id, dto);
    }

    @Operation(summary = "删除车位（逻辑删除，仅 AVAILABLE 可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("parking:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        parkingSpaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "分配车位给业主（AVAILABLE → OCCUPIED）")
    @PutMapping("/{id}/assign")
    @SaCheckPermission("parking:manage")
    public ParkingSpaceVO assign(@PathVariable String id, @Valid @RequestBody ParkingSpaceAssignDTO dto) {
        return parkingSpaceService.assign(id, dto);
    }

    @Operation(summary = "释放车位（OCCUPIED → AVAILABLE）")
    @PutMapping("/{id}/release")
    @SaCheckPermission("parking:manage")
    public ParkingSpaceVO release(@PathVariable String id) {
        return parkingSpaceService.release(id);
    }

    @Operation(summary = "我的车位列表")
    @GetMapping("/my")
    public List<ParkingSpaceVO> my() {
        return parkingSpaceService.my();
    }
}
