package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PollOptionVO {
    private String id;
    private String pollId;
    private String content;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
