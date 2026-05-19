package com.property.dto.request;

import lombok.Data;

@Data
public class ComplaintQueryDTO {
    private String status;
    private String tenantId;
    private long page = 1;
    private long pageSize = 20;
}
