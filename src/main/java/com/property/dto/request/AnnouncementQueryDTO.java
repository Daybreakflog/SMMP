package com.property.dto.request;

import lombok.Data;

@Data
public class AnnouncementQueryDTO {
    private String status;
    private String type;
    private String keyword;
    private long page = 1;
    private long pageSize = 20;
}
