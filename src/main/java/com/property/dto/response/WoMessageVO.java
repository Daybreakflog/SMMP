package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WoMessageVO {
    private String id;
    private String workOrderId;
    private String senderId;
    private String content;
    private String type;
    private LocalDateTime createdAt;
}
