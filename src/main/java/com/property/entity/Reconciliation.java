package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reconciliations")
public class Reconciliation {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private LocalDate period;
    private String projectId;
    private String channel;
    private BigDecimal totalAmount;
    private BigDecimal matchedAmount;
    private Integer unmatchedCount;
    private String status;
    private LocalDateTime reconciledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
