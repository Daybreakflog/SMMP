package com.property.dto.response;

import lombok.Data;

@Data
public class WorkOrderTrendVO {
    private String date;
    private Long newCount;
    private Long doneCount;
}
