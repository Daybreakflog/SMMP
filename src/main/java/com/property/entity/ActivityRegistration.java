package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_registrations")
public class ActivityRegistration {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String activityId;
    private String userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
