package com.property.controller;

import com.property.service.InspectionService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.InspectionPlanDTO;
import com.property.dto.request.InspectionPlanQueryDTO;
import com.property.dto.request.InspectionTaskCreateDTO;
import com.property.dto.response.InspectionPlanVO;
import com.property.dto.response.InspectionTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "InspectionPlan", description = "巡检计划管理")
@RestController
@RequestMapping("/inspection-plans")
@RequiredArgsConstructor
public class InspectionPlanController {

    private final InspectionService inspectionService;

    @Operation(summary = "新增巡检计划，返回 201")
    @PostMapping
    @SaCheckPermission("inspection:manage")
    public ResponseEntity<InspectionPlanVO> create(@Valid @RequestBody InspectionPlanDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.createPlan(dto));
    }

    @Operation(summary = "巡检计划列表（分页，?status&page&pageSize）")
    @GetMapping
    public PageResult<InspectionPlanVO> list(@ModelAttribute InspectionPlanQueryDTO query) {
        return inspectionService.planPage(query);
    }

    @Operation(summary = "计划详情")
    @GetMapping("/{id}")
    public InspectionPlanVO get(@PathVariable String id) {
        return inspectionService.getPlanById(id);
    }

    @Operation(summary = "编辑计划")
    @PutMapping("/{id}")
    @SaCheckPermission("inspection:manage")
    public InspectionPlanVO update(@PathVariable String id, @Valid @RequestBody InspectionPlanDTO dto) {
        return inspectionService.updatePlan(id, dto);
    }

    @Operation(summary = "删除计划（逻辑删除，仅 DRAFT 可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("inspection:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        inspectionService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "启用计划（DRAFT → ACTIVE）")
    @PutMapping("/{id}/activate")
    @SaCheckPermission("inspection:manage")
    public InspectionPlanVO activate(@PathVariable String id) {
        return inspectionService.activatePlan(id);
    }

    @Operation(summary = "停用计划（ACTIVE → DISABLED）")
    @PutMapping("/{id}/disable")
    @SaCheckPermission("inspection:manage")
    public InspectionPlanVO disable(@PathVariable String id) {
        return inspectionService.disablePlan(id);
    }

    @Operation(summary = "创建巡检任务（基于计划生成），返回 201")
    @PostMapping("/{planId}/tasks")
    @SaCheckPermission("inspection:manage")
    public ResponseEntity<InspectionTaskVO> createTask(
            @PathVariable String planId,
            @Valid @RequestBody InspectionTaskCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.createTask(planId, dto));
    }
}
