package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_plans")
public class InspectionPlan extends BaseEntity {

    private String name;
    private String description;
    private String route;
    /** DAILY | WEEKLY | MONTHLY */
    private String frequency;
    /** DRAFT | ACTIVE | DISABLED */
    private String status;
}
