package com.property.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractDTO {
    private String contractNo;
    private String title;
    /** LEASE | PROPERTY_SERVICE | PARKING | OTHER */
    private String type;
    private String tenantId;
    private String tenantName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private String remark;
}
