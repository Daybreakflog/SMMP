package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {
    private String id;
    private String title;
    private String content;
    private String type;
    private String status;
    private String authorId;
    private String projectId;
    private Boolean pinned;
    private LocalDateTime publishedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
