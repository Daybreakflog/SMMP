package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrderVO {
    private String id;
    private String no;
    private String category;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String tenantId;
    private String unitId;
    private String projectId;
    private String maintainerId;
    private LocalDateTime slaDueAt;
    private LocalDateTime completedAt;
    private Byte rating;
    private String ratingText;
    private String images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
