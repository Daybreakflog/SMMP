package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("facility_maintenance_records")
public class FacilityMaintenanceRecord extends BaseEntity {

    private String facilityId;
    /** ROUTINE | REPAIR | INSPECTION */
    private String type;
    private String description;
    private String maintainedBy;
    private LocalDateTime maintainedAt;
    private BigDecimal cost;
    private String remark;
}
