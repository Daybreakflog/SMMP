package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateDTO {
    @NotBlank
    private String code;
    @NotBlank
    private String type;
    @NotBlank
    private String name;
    private String title;
    private String content;
    private String paramsJson;
    private Boolean enabled;
}
