package com.property.dto.request;

import lombok.Data;

@Data
public class FacilityQueryDTO {

    private String status;
    private String category;
    private long page = 1;
    private long pageSize = 20;
}
