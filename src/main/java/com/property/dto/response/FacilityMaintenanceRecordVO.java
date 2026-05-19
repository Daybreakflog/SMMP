package com.property.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FacilityMaintenanceRecordVO {

    private String id;
    private String facilityId;
    private String type;
    private String description;
    private String maintainedBy;
    private LocalDateTime maintainedAt;
    private BigDecimal cost;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
