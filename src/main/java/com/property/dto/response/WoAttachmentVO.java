package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WoAttachmentVO {
    private String id;
    private String workOrderId;
    private String name;
    private String url;
    private Long size;
    private LocalDateTime createdAt;
}
