package com.property.controller;

import com.property.service.AnnouncementService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.AnnouncementDTO;
import com.property.dto.request.AnnouncementQueryDTO;
import com.property.dto.response.AnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Announcement", description = "公告管理")
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告列表（分页，?status&type&page&pageSize）")
    @GetMapping
    public PageResult<AnnouncementVO> list(@ModelAttribute AnnouncementQueryDTO query) {
        return announcementService.page(query);
    }

    @Operation(summary = "当前有效公告（公开，不分页，最近20条）")
    @GetMapping("/active")
    public List<AnnouncementVO> active() {
        return announcementService.active();
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public AnnouncementVO get(@PathVariable String id) {
        return announcementService.getById(id);
    }

    @Operation(summary = "新建公告，返回 201")
    @PostMapping
    @SaCheckPermission("announcement:manage")
    public ResponseEntity<AnnouncementVO> create(@RequestBody AnnouncementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.create(dto));
    }

    @Operation(summary = "更新公告（仅 DRAFT）")
    @PutMapping("/{id}")
    @SaCheckPermission("announcement:manage")
    public AnnouncementVO update(@PathVariable String id, @RequestBody AnnouncementDTO dto) {
        return announcementService.patch(id, dto);
    }

    @Operation(summary = "发布公告（DRAFT → PUBLISHED）")
    @PutMapping("/{id}/publish")
    @SaCheckPermission("announcement:manage")
    public AnnouncementVO publish(@PathVariable String id) {
        return announcementService.publish(id);
    }

    @Operation(summary = "撤回公告（PUBLISHED → REVOKED）")
    @PutMapping("/{id}/revoke")
    @SaCheckPermission("announcement:manage")
    public AnnouncementVO revoke(@PathVariable String id) {
        return announcementService.revoke(id);
    }

    @Operation(summary = "删除公告（逻辑删除），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("announcement:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
