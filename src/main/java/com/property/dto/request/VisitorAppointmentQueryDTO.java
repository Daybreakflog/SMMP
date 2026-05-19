package com.property.dto.request;

import lombok.Data;

@Data
public class VisitorAppointmentQueryDTO {
    private String status;
    private long page = 1;
    private long pageSize = 20;
}
