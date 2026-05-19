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
@TableName("bill_items")
public class BillItem {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String billId;
    private String feeItemId;
    private String feeItemName;
    private String type;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal meterStart;
    private BigDecimal meterEnd;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
