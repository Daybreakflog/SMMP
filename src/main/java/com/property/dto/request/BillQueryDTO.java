package com.property.dto.request;

import lombok.Data;

@Data
public class BillQueryDTO {
    private int page = 1;
    private int pageSize = 20;
    private String status;
    private String period;
    private String tenantId;
}
