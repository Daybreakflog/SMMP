package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseDTO {
    @NotBlank
    private String period;
    @NotBlank
    private String projectId;
    /** 必须为 "我已确认" 才能执行关账 */
    @NotBlank
    private String confirm;
    private String notes;
}
