package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExportTaskVO {
    private String id;
    private String type;
    private String status;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
}
