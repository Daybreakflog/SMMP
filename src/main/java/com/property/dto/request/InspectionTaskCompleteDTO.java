package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InspectionTaskCompleteDTO {

    @NotBlank(message = "巡检结果不能为空")
    private String result;

    private String remark;
}
