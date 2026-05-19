package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("export_tasks")
public class ExportTask {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String type;
    private String paramsJson;
    /** PENDING | RUNNING | DONE | FAILED */
    private String status;
    private String fileUrl;
    private String operatorId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
