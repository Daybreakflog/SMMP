package com.property.dto.request;

import lombok.Data;

@Data
public class WorkOrderQueryDTO {
    private String type;
    private String status;
    private String assigneeId;
    private long page = 1;
    private long pageSize = 20;
}
