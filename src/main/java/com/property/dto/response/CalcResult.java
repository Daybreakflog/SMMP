package com.property.dto.response;

import java.math.BigDecimal;

public record CalcResult(
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        BigDecimal meterStart,
        BigDecimal meterEnd
) {}
