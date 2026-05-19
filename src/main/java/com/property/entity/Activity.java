package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activities")
public class Activity extends BaseEntity {
    private String title;
    private String description;
    private String location;
    /** DRAFT | PUBLISHED | CLOSED */
    private String status;
    private String authorId;
    private Integer maxParticipants;
    private LocalDateTime registerDeadline;
    private LocalDateTime activityStartAt;
    private LocalDateTime activityEndAt;
}
