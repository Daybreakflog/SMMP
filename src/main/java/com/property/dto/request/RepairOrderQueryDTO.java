package com.property.dto.request;

import lombok.Data;

@Data
public class RepairOrderQueryDTO {

    private String status;
    private String reporterId;
    private long page = 1;
    private long pageSize = 20;
}
