package com.property.dto.request;

import lombok.Data;

@Data
public class SysConfigDTO {
    private String key;
    private String value;
    private String description;
}
