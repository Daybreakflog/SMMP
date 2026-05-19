package com.property.controller;

import com.property.service.InspectionService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.InspectionTaskCompleteDTO;
import com.property.dto.request.InspectionTaskQueryDTO;
import com.property.dto.response.InspectionTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "InspectionTask", description = "巡检任务管理")
@RestController
@RequestMapping("/inspection-tasks")
@RequiredArgsConstructor
public class InspectionTaskController {

    private final InspectionService inspectionService;

    @Operation(summary = "巡检任务列表（分页，?status&assigneeId&page&pageSize）")
    @GetMapping
    public PageResult<InspectionTaskVO> list(@ModelAttribute InspectionTaskQueryDTO query) {
        return inspectionService.taskPage(query);
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public InspectionTaskVO get(@PathVariable String id) {
        return inspectionService.getTaskById(id);
    }

    @Operation(summary = "开始巡检（PENDING → IN_PROGRESS）")
    @PutMapping("/{id}/start")
    @SaCheckPermission("inspection:execute")
    public InspectionTaskVO start(@PathVariable String id) {
        return inspectionService.startTask(id);
    }

    @Operation(summary = "完成巡检（IN_PROGRESS → COMPLETED，提交巡检结果）")
    @PutMapping("/{id}/complete")
    @SaCheckPermission("inspection:execute")
    public InspectionTaskVO complete(@PathVariable String id,
                                     @Valid @RequestBody InspectionTaskCompleteDTO dto) {
        return inspectionService.completeTask(id, dto);
    }
}
