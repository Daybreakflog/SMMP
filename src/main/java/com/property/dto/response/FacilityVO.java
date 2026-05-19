package com.property.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FacilityVO {

    private String id;
    private String name;
    private String category;
    private String location;
    private String status;
    private LocalDate installDate;
    private LocalDateTime lastMaintenanceAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
