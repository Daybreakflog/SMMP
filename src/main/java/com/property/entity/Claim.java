package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("claims")
public class Claim {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String paymentRecordId;
    private String billId;
    private BigDecimal amount;
    private Boolean autoClaimed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
