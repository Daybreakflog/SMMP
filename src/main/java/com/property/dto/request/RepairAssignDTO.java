package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairAssignDTO {

    @NotBlank
    private String assigneeId;

    @NotBlank
    private String assigneeName;
}
