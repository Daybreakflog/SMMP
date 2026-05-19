package com.property.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractRenewDTO {
    private LocalDate endDate;
}
