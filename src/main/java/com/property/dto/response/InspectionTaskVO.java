package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionTaskVO {

    private String id;
    private String planId;
    private String assigneeId;
    private String assigneeName;
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String result;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
