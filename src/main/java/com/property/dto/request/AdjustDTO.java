package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustDTO {
    @NotBlank
    private String paymentRecordId;
    @NotBlank
    private String billId;
    @NotNull
    private BigDecimal amount;
    private String remark;
}
