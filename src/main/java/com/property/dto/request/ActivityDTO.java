package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityDTO {
    @NotBlank
    private String title;
    private String description;
    private String location;
    private Integer maxParticipants;
    private LocalDateTime registerDeadline;
    private LocalDateTime activityStartAt;
    private LocalDateTime activityEndAt;
}
