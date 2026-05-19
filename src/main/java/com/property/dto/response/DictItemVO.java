package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictItemVO {
    private String id;
    private String type;
    private String code;
    private String label;
    private Integer sort;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
