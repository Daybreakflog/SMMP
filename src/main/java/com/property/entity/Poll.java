package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("polls")
public class Poll extends BaseEntity {
    private String title;
    private String description;
    /** SINGLE_CHOICE | MULTIPLE_CHOICE */
    private String type;
    /** DRAFT | PUBLISHED | CLOSED */
    private String status;
    private String authorId;
    private LocalDateTime deadline;
}
