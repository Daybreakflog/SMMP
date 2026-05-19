package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_tasks")
public class InspectionTask extends BaseEntity {

    private String planId;
    private String assigneeId;
    private String assigneeName;
    /** PENDING | IN_PROGRESS | COMPLETED */
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String result;
    private String remark;
}
