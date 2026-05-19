package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollVO {
    private String id;
    private String title;
    private String description;
    private String type;
    private String status;
    private String authorId;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PollOptionVO> options;
}
