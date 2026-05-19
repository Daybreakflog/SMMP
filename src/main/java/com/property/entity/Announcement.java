package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcements")
public class Announcement extends BaseEntity {
    private String title;
    private String content;
    /** NOTICE | ACTIVITY | POLICY */
    private String type;
    /** DRAFT | PUBLISHED | REVOKED */
    private String status;
    private String authorId;
    private String projectId;
    private Boolean pinned;
    private LocalDateTime publishedAt;
    private LocalDateTime expiredAt;
}
