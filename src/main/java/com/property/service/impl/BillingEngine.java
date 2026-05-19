package com.property.service.impl;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;
import com.property.service.strategy.BillingStrategy;

import com.property.entity.*;
import com.property.mapper.BillItemMapper;
import com.property.mapper.BillMapper;
import com.property.mapper.FeeTierMapper;
import com.property.mapper.UnitMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillingEngine {

    private final Map<String, BillingStrategy> strategies;
    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final FeeTierMapper feeTierMapper;
    private final UnitMapper unitMapper;

    @Transactional
    public Bill generateBill(Contract contract, List<FeeItem> feeItems, String period) {
        Unit unit = unitMapper.selectById(contract.getUnitId());

        Bill bill = new Bill();
        bill.setNo("BILL-" + period.replace("-", "") + "-" + contract.getId().substring(0, 6).toUpperCase());
        bill.setPeriod(period);
        bill.setContractId(contract.getId());
        bill.setTenantId(contract.getTenantId());
        bill.setUnitId(contract.getUnitId());
        bill.setProjectId(unit.getProjectId());
        bill.setStatus("UNPAID");
        bill.setTotalAmount(BigDecimal.ZERO);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setDueDate(LocalDate.parse(period + "-15"));
        billMapper.insert(bill);

        List<BillItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (FeeItem feeItem : feeItems) {
            List<FeeTier> tiers = feeTierMapper.selectList(
                    new LambdaQueryWrapper<FeeTier>().eq(FeeTier::getFeeItemId, feeItem.getId()));

            BillingStrategy strategy = strategies.get(feeItem.getType());
            if (strategy == null) continue;

            BillingContext ctx = new BillingContext(feeItem, tiers, unit,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            CalcResult result = strategy.calculate(ctx);

            BillItem item = new BillItem();
            item.setBillId(bill.getId());
            item.setFeeItemId(feeItem.getId());
            item.setFeeItemName(feeItem.getName());
            item.setType(feeItem.getType());
            item.setQuantity(result.quantity());
            item.setUnitPrice(result.unitPrice());
            item.setAmount(result.amount());
            item.setMeterStart(result.meterStart());
            item.setMeterEnd(result.meterEnd());
            billItemMapper.insert(item);
            items.add(item);
            total = total.add(result.amount());
        }

        bill.setTotalAmount(total);
        billMapper.updateById(bill);
        return bill;
    }
}
