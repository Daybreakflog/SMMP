package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("facilities")
public class Facility extends BaseEntity {

    private String name;
    /** ELEVATOR | FIRE_EQUIPMENT | WATER_SUPPLY | ELECTRICAL | OTHER */
    private String category;
    private String location;
    /** NORMAL | MAINTENANCE | SCRAPPED */
    private String status;
    private LocalDate installDate;
    private LocalDateTime lastMaintenanceAt;
    private String remark;
}
