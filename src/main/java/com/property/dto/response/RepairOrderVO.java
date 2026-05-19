package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairOrderVO {

    private String id;
    private String title;
    private String description;
    private String location;
    private String reporterId;
    private String reporterName;
    private String assigneeId;
    private String assigneeName;
    private String status;
    private String result;
    private String rejectReason;
    private Integer rating;
    private String ratingComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
