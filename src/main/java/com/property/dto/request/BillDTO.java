package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillDTO {
    @NotBlank
    private String contractId;
    @NotBlank
    private String tenantId;
    @NotBlank
    private String unitId;
    @NotBlank
    private String projectId;
    @NotBlank
    private String period;
    @NotNull
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private String remark;
}
