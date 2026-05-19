package com.property.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementDTO {
    private String title;
    private String content;
    private String type;
    private String projectId;
    private Boolean pinned;
    private LocalDateTime expiredAt;
}
