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
@TableName("fee_tiers")
public class FeeTier {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String feeItemId;
    private BigDecimal minQty;
    private BigDecimal maxQty;
    private BigDecimal unitPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
