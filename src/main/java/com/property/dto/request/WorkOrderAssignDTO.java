package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkOrderAssignDTO {
    @NotBlank
    private String assigneeId;
}
