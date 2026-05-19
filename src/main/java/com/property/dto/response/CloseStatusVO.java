package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CloseStatusVO {
    private boolean closed;
    private LocalDateTime closedAt;
    private String by;
    private String period;
    private String projectId;
}
