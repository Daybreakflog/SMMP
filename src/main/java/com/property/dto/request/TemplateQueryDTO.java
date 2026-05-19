package com.property.dto.request;

import lombok.Data;

@Data
public class TemplateQueryDTO {
    private long page = 1;
    private long pageSize = 20;
    private String type;
    private String name;
    private String code;
}
