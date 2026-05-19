package com.property.controller;

import com.property.service.NotificationService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.common.api.PageResult;
import com.property.dto.request.NotificationQueryDTO;
import com.property.dto.request.SendNotificationDTO;
import com.property.dto.response.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "通知管理")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "发送通知（指定接收人或全体）")
    @PostMapping
    @SaCheckPermission("notification:manage")
    public ResponseEntity<Void> send(@Valid @RequestBody SendNotificationDTO dto) {
        notificationService.send(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "我的通知列表（分页）")
    @GetMapping
    public PageResult<NotificationVO> my(@ModelAttribute NotificationQueryDTO query) {
        return notificationService.my(query);
    }

    @Operation(summary = "通知详情（自动标记已读）")
    @GetMapping("/{id}")
    public NotificationVO getById(@PathVariable String id) {
        return notificationService.getById(id);
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable String id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "标记全部已读")
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public long unreadCount() {
        return notificationService.unreadCount();
    }

    @Operation(summary = "删除通知（仅删自己的）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
