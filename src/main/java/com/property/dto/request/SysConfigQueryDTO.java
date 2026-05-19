package com.property.dto.request;

import lombok.Data;

@Data
public class SysConfigQueryDTO {
    private String groupCode;
    private String keyword;
    private long page = 1;
    private long pageSize = 20;
}
