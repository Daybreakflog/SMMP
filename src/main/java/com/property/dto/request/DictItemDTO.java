package com.property.dto.request;

import lombok.Data;

@Data
public class DictItemDTO {
    private String type;
    private String code;
    private String label;
    private Integer sort;
    private Boolean enabled;
}
