package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("complaint_timelines")
public class ComplaintTimeline {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String complaintId;
    private String action;
    private String content;
    private String operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
