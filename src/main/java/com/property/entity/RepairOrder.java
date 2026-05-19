package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair_orders")
public class RepairOrder extends BaseEntity {

    private String title;
    private String description;
    private String location;
    private String reporterId;
    private String reporterName;
    private String assigneeId;
    private String assigneeName;
    /** PENDING | ASSIGNED | IN_PROGRESS | COMPLETED | CONFIRMED | CANCELLED */
    private String status;
    private String result;
    private String rejectReason;
    private Integer rating;
    private String ratingComment;
}
