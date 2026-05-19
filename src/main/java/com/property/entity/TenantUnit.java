package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 入住历史（无逻辑删除，无 updated_at） */
@Data
@TableName("tenant_units")
public class TenantUnit {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String tenantId;
    private String unitId;
    private String contractId;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
