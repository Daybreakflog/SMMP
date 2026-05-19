package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fee_items")
public class FeeItem extends BaseEntity {
    private String name;
    /** FIXED | BY_AREA | BY_METER | TIERED */
    private String type;
    private BigDecimal fixedAmount;
    private BigDecimal unitPrice;
    private String projectId;
    private String status;
}
