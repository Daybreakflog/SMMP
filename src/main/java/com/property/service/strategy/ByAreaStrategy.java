package com.property.service.strategy;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("BY_AREA")
public class ByAreaStrategy implements BillingStrategy {

    @Override
    public CalcResult calculate(BillingContext ctx) {
        BigDecimal area = ctx.unit().getArea();
        BigDecimal unitPrice = ctx.feeItem().getUnitPrice();
        BigDecimal amount = area.multiply(unitPrice);
        return new CalcResult(area, unitPrice, amount, null, null);
    }
}
