package com.property.dto.request;

import lombok.Data;

@Data
public class NotificationMessage {
    private String userId;
    private String type;
    private String channel;
    private String title;
    private String content;
    private String targetId;
    private String targetType;
}
