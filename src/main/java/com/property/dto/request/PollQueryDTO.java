package com.property.dto.request;

import lombok.Data;

@Data
public class PollQueryDTO {
    private String status;
    private long page = 1;
    private long pageSize = 20;
}
