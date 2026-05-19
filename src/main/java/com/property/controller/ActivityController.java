package com.property.controller;

import com.property.service.ActivityService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.ActivityDTO;
import com.property.dto.request.ActivityQueryDTO;
import com.property.dto.response.ActivityVO;
import com.property.dto.response.ParticipantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Activity", description = "活动报名管理")
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "创建活动（草稿状态），返回 201")
    @PostMapping
    @SaCheckPermission("activity:manage")
    public ResponseEntity<ActivityVO> create(@Valid @RequestBody ActivityDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(dto));
    }

    @Operation(summary = "活动列表（分页，?status&page&pageSize）")
    @GetMapping
    public PageResult<ActivityVO> list(@ModelAttribute ActivityQueryDTO query) {
        return activityService.page(query);
    }

    @Operation(summary = "活动详情（含已报名人数）")
    @GetMapping("/{id}")
    public ActivityVO get(@PathVariable String id) {
        return activityService.getById(id);
    }

    @Operation(summary = "编辑活动（仅草稿状态可编辑）")
    @PutMapping("/{id}")
    @SaCheckPermission("activity:manage")
    public ActivityVO update(@PathVariable String id, @Valid @RequestBody ActivityDTO dto) {
        return activityService.update(id, dto);
    }

    @Operation(summary = "发布活动（DRAFT → PUBLISHED）")
    @PutMapping("/{id}/publish")
    @SaCheckPermission("activity:manage")
    public ActivityVO publish(@PathVariable String id) {
        return activityService.publish(id);
    }

    @Operation(summary = "关闭活动（PUBLISHED → CLOSED）")
    @PutMapping("/{id}/close")
    @SaCheckPermission("activity:manage")
    public ActivityVO close(@PathVariable String id) {
        return activityService.close(id);
    }

    @Operation(summary = "删除活动（逻辑删除，仅草稿可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("activity:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "报名参加活动，返回 204")
    @PostMapping("/{id}/register")
    public ResponseEntity<Void> register(@PathVariable String id) {
        activityService.register(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "取消报名，返回 204")
    @DeleteMapping("/{id}/register")
    public ResponseEntity<Void> cancelRegister(@PathVariable String id) {
        activityService.cancelRegister(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "参与者列表（分页）")
    @GetMapping("/{id}/participants")
    public PageResult<ParticipantVO> participants(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return activityService.participants(id, page, pageSize);
    }
}
