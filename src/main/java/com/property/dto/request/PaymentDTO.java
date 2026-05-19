package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDTO {
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String method;
    private String externalId;
}
