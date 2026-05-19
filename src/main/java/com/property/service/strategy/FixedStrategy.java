package com.property.service.strategy;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("FIXED")
public class FixedStrategy implements BillingStrategy {

    @Override
    public CalcResult calculate(BillingContext ctx) {
        BigDecimal amount = ctx.feeItem().getFixedAmount();
        return new CalcResult(BigDecimal.ONE, amount, amount, null, null);
    }
}
