package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysConfigVO {
    private String id;
    private String key;
    private String value;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
