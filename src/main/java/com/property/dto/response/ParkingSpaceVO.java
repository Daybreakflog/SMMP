package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParkingSpaceVO {
    private String id;
    private String spaceNo;
    private String zone;
    private String status;
    private String ownerId;
    private String ownerName;
    private String vehiclePlate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
