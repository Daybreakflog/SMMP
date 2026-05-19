package com.property.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollDTO {
    private String title;
    private String description;
    /** SINGLE_CHOICE | MULTIPLE_CHOICE */
    private String type;
    private LocalDateTime deadline;
    private List<PollOptionItem> options;

    @Data
    public static class PollOptionItem {
        private String content;
        private Integer sortOrder;
    }
}
