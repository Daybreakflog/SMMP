package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FacilityMaintenanceRecordDTO {

    @NotBlank
    @Pattern(regexp = "ROUTINE|REPAIR|INSPECTION",
            message = "类型只能是 ROUTINE、REPAIR 或 INSPECTION")
    private String type;

    @NotBlank
    private String description;

    private String maintainedBy;
    private LocalDateTime maintainedAt;
    private BigDecimal cost;
    private String remark;
}
