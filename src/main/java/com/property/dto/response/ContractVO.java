package com.property.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractVO {
    private String id;
    private String contractNo;
    private String title;
    private String type;
    private String tenantId;
    private String tenantName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private String status;
    private String rejectReason;
    private String terminateReason;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
