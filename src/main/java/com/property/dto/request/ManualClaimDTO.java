package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ManualClaimDTO {
    @NotBlank
    private String paymentId;
    @NotEmpty
    private List<Allocation> allocations;

    @Data
    public static class Allocation {
        private String billId;
        private BigDecimal amount;
    }
}
