package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wo_timelines")
public class WoTimeline {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String workOrderId;
    private String action;
    private String content;
    private String operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
