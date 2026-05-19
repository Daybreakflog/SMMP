package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contracts")
public class Contract extends BaseEntity {
    @TableField("no")
    private String contractNo;
    private String title;
    /** LEASE | PROPERTY_SERVICE | PARKING | OTHER */
    private String type;
    private String tenantId;
    private String tenantName;
    private String unitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    /** DRAFT | PENDING_APPROVAL | ACTIVE | TERMINATED | EXPIRED */
    private String status;
    private String rejectReason;
    private String terminateReason;
    private String remark;
}
