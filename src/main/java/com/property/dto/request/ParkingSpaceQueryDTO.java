package com.property.dto.request;

import lombok.Data;

@Data
public class ParkingSpaceQueryDTO {
    private String status;
    private String zone;
    private long page = 1;
    private long pageSize = 20;
}
