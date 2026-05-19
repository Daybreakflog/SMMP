package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InspectionPlanDTO {

    @NotBlank(message = "计划名称不能为空")
    private String name;

    private String description;

    private String route;

    @NotBlank(message = "巡检频率不能为空")
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY",
            message = "频率只能是 DAILY、WEEKLY 或 MONTHLY")
    private String frequency;
}
