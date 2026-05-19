package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("complaints")
public class Complaint extends BaseEntity {
    private String no;
    private String title;
    private String content;
    private String category;
    /** PENDING | HANDLING | RESOLVED | CLOSED */
    private String status;
    private String tenantId;
    private String unitId;
    private String projectId;
    private String handlerId;
    private LocalDateTime resolvedAt;
}
