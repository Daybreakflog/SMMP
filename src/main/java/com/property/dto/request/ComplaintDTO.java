package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintDTO {
    @NotBlank
    private String title;
    private String content;
    private String category;
    private String tenantId;
    private String unitId;
    private String projectId;
}
