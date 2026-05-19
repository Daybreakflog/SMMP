package com.property.dto.request;

import com.property.entity.FeeItem;
import com.property.entity.FeeTier;
import com.property.entity.Unit;

import java.math.BigDecimal;
import java.util.List;

public record BillingContext(
        FeeItem feeItem,
        List<FeeTier> tiers,
        Unit unit,
        BigDecimal prevMeter,
        BigDecimal curMeter
) {}
