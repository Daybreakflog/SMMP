package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClaimRuleVO {
    private String id;
    private String name;
    private Integer priority;
    private String conditionJson;
    private String actionJson;
    private Boolean enabled;
    private String projectId;
    private LocalDateTime createdAt;
}
