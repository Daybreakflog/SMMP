package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_records")
public class PaymentRecord extends BaseEntity {
    private BigDecimal amount;
    private String channel;
    private String externalId;
    private String tenantId;
    /** PENDING | MATCHED | PARTIAL | UNMATCHED */
    private String status;
    private LocalDateTime receivedAt;
    private LocalDateTime reconciledAt;
    private LocalDateTime claimedAt;
    private String remark;
}
