package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionPlanVO {

    private String id;
    private String name;
    private String description;
    private String route;
    private String frequency;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
