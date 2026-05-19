package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionTaskCreateDTO {

    @NotBlank(message = "执行人ID不能为空")
    private String assigneeId;

    @NotBlank(message = "执行人姓名不能为空")
    private String assigneeName;

    @NotNull(message = "计划执行时间不能为空")
    private LocalDateTime scheduledAt;
}
