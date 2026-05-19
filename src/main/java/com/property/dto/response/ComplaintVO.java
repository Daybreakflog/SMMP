package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintVO {
    private String id;
    private String no;
    private String title;
    private String content;
    private String category;
    private String status;
    private String tenantId;
    private String unitId;
    private String projectId;
    private String handlerId;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
