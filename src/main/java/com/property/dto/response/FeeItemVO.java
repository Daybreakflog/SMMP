package com.property.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeItemVO {
    private String id;
    private String name;
    private String type;
    private BigDecimal fixedAmount;
    private BigDecimal unitPrice;
    private String projectId;
    private String status;
    private LocalDateTime createdAt;
}
