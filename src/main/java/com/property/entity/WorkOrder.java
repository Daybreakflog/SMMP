package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_orders")
public class WorkOrder extends BaseEntity {
    private String no;
    private String category;
    private String title;
    private String description;
    /** PENDING|ASSIGNED|IN_PROGRESS|DONE|CLOSED|CANCELLED */
    private String status;
    private String priority;
    private String tenantId;
    private String unitId;
    private String projectId;
    private String maintainerId;
    private LocalDateTime slaDueAt;
    private LocalDateTime completedAt;
    private LocalDateTime ratedAt;
    private Byte rating;
    private String ratingText;
    /** JSON 数组：图片 URL 列表 */
    private String images;
}
