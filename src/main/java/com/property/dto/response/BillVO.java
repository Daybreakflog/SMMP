package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillVO {
    private String id;
    private String no;
    private String period;
    private String contractId;
    private String tenantId;
    private String unitId;
    private String projectId;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal totalAmount;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal paidAmount;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private LocalDateTime voidedAt;
    private LocalDateTime createdAt;
}
