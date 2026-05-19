package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private String targetId;
    private String targetType;

    private String channel;   // IN_APP | SMS | PUSH
    private String status;    // PENDING | SENT | FAILED
    private LocalDateTime readAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
