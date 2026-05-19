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
@TableName("closes")
public class PeriodClose {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String period;
    private String projectId;
    private String closedBy;
    private LocalDateTime closedAt;
    private BigDecimal totalRevenue;
    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
