package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wo_attachments")
public class WoAttachment {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String workOrderId;
    private String name;
    private String url;
    private Long size;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
