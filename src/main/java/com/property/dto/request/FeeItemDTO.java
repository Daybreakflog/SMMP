package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeItemDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String type;
    private BigDecimal fixedAmount;
    private BigDecimal unitPrice;
    private String projectId;
    private String status;
}
