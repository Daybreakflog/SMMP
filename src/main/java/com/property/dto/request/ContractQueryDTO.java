package com.property.dto.request;

import lombok.Data;

@Data
public class ContractQueryDTO {
    private String status;
    private String tenantId;
    private String type;
    private long page = 1;
    private long pageSize = 20;
}
