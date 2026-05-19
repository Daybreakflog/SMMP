package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单（分区表）。分区键 due_date 与 id 共同构成 PK。
 * MyBatis-Plus 以 id 作为逻辑主键，due_date 对 MP 透明。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bills")
public class Bill extends BaseEntity {
    private String no;
    private String period;
    private String contractId;
    private String tenantId;
    private String unitId;
    private String projectId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    /** UNPAID | PARTIAL | PAID | OVERDUE | VOID */
    private String status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private LocalDateTime voidedAt;
}
