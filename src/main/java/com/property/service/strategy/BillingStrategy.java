package com.property.service.strategy;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;

public interface BillingStrategy {
    CalcResult calculate(BillingContext context);
}
