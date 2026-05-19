package com.property.service.strategy;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;

import com.property.entity.FeeTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component("TIERED")
public class TieredStrategy implements BillingStrategy {

    @Override
    public CalcResult calculate(BillingContext ctx) {
        BigDecimal qty = ctx.curMeter().subtract(ctx.prevMeter());
        List<FeeTier> tiers = ctx.tiers().stream()
                .sorted(Comparator.comparing(FeeTier::getMinQty))
                .toList();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal remaining = qty;

        for (FeeTier tier : tiers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal tierMax = tier.getMaxQty() != null
                    ? tier.getMaxQty().subtract(tier.getMinQty())
                    : remaining;
            BigDecimal inTier = remaining.min(tierMax);
            total = total.add(inTier.multiply(tier.getUnitPrice()));
            remaining = remaining.subtract(inTier);
        }

        return new CalcResult(qty, null, total, ctx.prevMeter(), ctx.curMeter());
    }
}
