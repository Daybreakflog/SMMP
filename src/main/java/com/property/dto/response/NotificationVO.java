package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private String id;
    private String userId;
    private String type;
    private String channel;
    private String status;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String targetId;
    private String targetType;
    private LocalDateTime createdAt;
}
