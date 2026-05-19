package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkOrderDTO {
    @NotBlank
    private String title;
    private String description;
    private String category;
    private String priority;
    private String tenantId;
    private String unitId;
    private String projectId;
    private String images;
}
