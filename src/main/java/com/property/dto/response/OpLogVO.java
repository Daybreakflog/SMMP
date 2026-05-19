package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpLogVO {
    private Long id;
    private String actorId;
    private String actorName;
    private String action;
    private String target;
    private String diff;
    private String ip;
    private String userAgent;
    private String traceId;
    private LocalDateTime at;
}
