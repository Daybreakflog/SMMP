package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wo_messages")
public class WoMessage {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String workOrderId;
    private String senderId;
    private String content;
    /** TEXT | IMAGE | FILE */
    private String type;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
