package com.property.dto.request;

import lombok.Data;

@Data
public class InspectionPlanQueryDTO {

    private String status;
    private long page = 1;
    private long pageSize = 20;
}
