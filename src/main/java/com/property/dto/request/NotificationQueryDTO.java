package com.property.dto.request;

import lombok.Data;

@Data
public class NotificationQueryDTO {
    private long page = 1;
    private long pageSize = 20;
    private String type;
    private Boolean read;
}
