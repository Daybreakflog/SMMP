package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemplateVO {
    private String id;
    private String code;
    private String type;
    private String name;
    private String title;
    private String content;
    private String paramsJson;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
