package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVO {
    private String id;
    private String title;
    private String description;
    private String location;
    private String status;
    private String authorId;
    private Integer maxParticipants;
    private LocalDateTime registerDeadline;
    private LocalDateTime activityStartAt;
    private LocalDateTime activityEndAt;
    private Integer registeredCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
