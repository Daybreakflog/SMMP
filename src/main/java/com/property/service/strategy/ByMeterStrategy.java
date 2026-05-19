package com.property.service.strategy;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("BY_METER")
public class ByMeterStrategy implements BillingStrategy {

    @Override
    public CalcResult calculate(BillingContext ctx) {
        BigDecimal qty = ctx.curMeter().subtract(ctx.prevMeter());
        BigDecimal unitPrice = ctx.feeItem().getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);
        return new CalcResult(qty, unitPrice, amount, ctx.prevMeter(), ctx.curMeter());
    }
}
