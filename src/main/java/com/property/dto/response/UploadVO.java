package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UploadVO {
    private String id;
    private String name;
    private Long size;
    private String mimeType;
    private String url;
    private String ossKey;
    private String creatorId;
    private LocalDateTime createdAt;
}
