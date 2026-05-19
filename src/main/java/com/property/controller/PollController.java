package com.property.controller;

import com.property.service.PollService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.PollDTO;
import com.property.dto.request.PollQueryDTO;
import com.property.dto.request.VoteDTO;
import com.property.dto.response.PollResultVO;
import com.property.dto.response.PollVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Poll", description = "投票/问卷管理")
@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @Operation(summary = "投票列表（分页，?status&page&pageSize）")
    @GetMapping
    public PageResult<PollVO> list(@ModelAttribute PollQueryDTO query) {
        return pollService.page(query);
    }

    @Operation(summary = "投票详情（含选项列表）")
    @GetMapping("/{id}")
    public PollVO get(@PathVariable String id) {
        return pollService.getById(id);
    }

    @Operation(summary = "创建投票/问卷（草稿状态），返回 201")
    @PostMapping
    @SaCheckPermission("poll:manage")
    public ResponseEntity<PollVO> create(@RequestBody PollDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pollService.create(dto));
    }

    @Operation(summary = "编辑投票（仅草稿状态可编辑）")
    @PutMapping("/{id}")
    @SaCheckPermission("poll:manage")
    public PollVO update(@PathVariable String id, @RequestBody PollDTO dto) {
        return pollService.update(id, dto);
    }

    @Operation(summary = "发布投票（DRAFT → PUBLISHED）")
    @PutMapping("/{id}/publish")
    @SaCheckPermission("poll:manage")
    public PollVO publish(@PathVariable String id) {
        return pollService.publish(id);
    }

    @Operation(summary = "关闭投票（PUBLISHED → CLOSED）")
    @PutMapping("/{id}/close")
    @SaCheckPermission("poll:manage")
    public PollVO close(@PathVariable String id) {
        return pollService.close(id);
    }

    @Operation(summary = "删除投票（逻辑删除，仅草稿可删），返回 204")
    @DeleteMapping("/{id}")
    @SaCheckPermission("poll:manage")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        pollService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "提交投票")
    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> vote(@PathVariable String id, @RequestBody VoteDTO dto) {
        pollService.vote(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "投票结果统计")
    @GetMapping("/{id}/result")
    public PollResultVO result(@PathVariable String id) {
        return pollService.result(id);
    }
}
