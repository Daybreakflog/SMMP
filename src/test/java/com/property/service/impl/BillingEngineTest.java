package com.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.dto.response.CalcResult;
import com.property.entity.*;
import com.property.mapper.BillItemMapper;
import com.property.mapper.BillMapper;
import com.property.mapper.FeeTierMapper;
import com.property.mapper.UnitMapper;
import com.property.service.strategy.BillingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingEngine Unit Tests")
class BillingEngineTest {

    @Mock private BillMapper billMapper;
    @Mock private BillItemMapper billItemMapper;
    @Mock private FeeTierMapper feeTierMapper;
    @Mock private UnitMapper unitMapper;
    @Mock private Map<String, BillingStrategy> strategies;
    @InjectMocks private BillingEngine billingEngine;

    private Contract contract;
    private Unit unit;

    @BeforeEach
    void setUp() {
        contract = new Contract();
        contract.setId("c00001-abcdef");
        contract.setTenantId("t001");
        contract.setUnitId("u001");

        unit = new Unit();
        unit.setId("u001");
        unit.setProjectId("p001");
        unit.setArea(new BigDecimal("120.00"));
    }

    @Nested @DisplayName("Single FeeItem")
    class SingleFeeItem {

        @Test @DisplayName("FIXED fee -> bill with correct total")
        void fixedFee() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);
            when(feeTierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            BillingStrategy fixedStrategy = mock(BillingStrategy.class);
            when(strategies.get("FIXED")).thenReturn(fixedStrategy);
            when(fixedStrategy.calculate(any())).thenReturn(
                    new CalcResult(BigDecimal.ONE, new BigDecimal("500.00"),
                            new BigDecimal("500.00"), null, null));
            when(billItemMapper.insert(any(BillItem.class))).thenReturn(1);

            FeeItem fee = new FeeItem();
            fee.setId("f001");
            fee.setName("Property Mgmt");
            fee.setType("FIXED");
            fee.setFixedAmount(new BigDecimal("500.00"));

            Bill bill = billingEngine.generateBill(contract, List.of(fee), "2026-03");

            assertEquals("UNPAID", bill.getStatus());
            assertEquals(0, new BigDecimal("500.00").compareTo(bill.getTotalAmount()));
            assertEquals(0, BigDecimal.ZERO.compareTo(bill.getPaidAmount()));
            verify(billItemMapper).insert(any(BillItem.class));
        }

        @Test @DisplayName("BY_AREA fee -> amount = area * unitPrice")
        void byAreaFee() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);
            when(feeTierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            BillingStrategy areaStrategy = mock(BillingStrategy.class);
            when(strategies.get("BY_AREA")).thenReturn(areaStrategy);
            when(areaStrategy.calculate(any())).thenReturn(
                    new CalcResult(new BigDecimal("120.00"), new BigDecimal("3.50"),
                            new BigDecimal("420.00"), null, null));
            when(billItemMapper.insert(any(BillItem.class))).thenReturn(1);

            FeeItem fee = new FeeItem();
            fee.setId("f002");
            fee.setName("Area Fee");
            fee.setType("BY_AREA");
            fee.setUnitPrice(new BigDecimal("3.50"));

            Bill bill = billingEngine.generateBill(contract, List.of(fee), "2026-03");
            assertEquals(0, new BigDecimal("420.00").compareTo(bill.getTotalAmount()));
        }
    }

    @Nested @DisplayName("Multiple FeeItems")
    class MultipleFeeItems {

        @Test @DisplayName("two fee items -> total is sum")
        void twoFees() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);
            when(feeTierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            BillingStrategy fixedStrategy = mock(BillingStrategy.class);
            BillingStrategy areaStrategy = mock(BillingStrategy.class);
            when(strategies.get("FIXED")).thenReturn(fixedStrategy);
            when(strategies.get("BY_AREA")).thenReturn(areaStrategy);
            when(fixedStrategy.calculate(any())).thenReturn(
                    new CalcResult(BigDecimal.ONE, new BigDecimal("200.00"),
                            new BigDecimal("200.00"), null, null));
            when(areaStrategy.calculate(any())).thenReturn(
                    new CalcResult(new BigDecimal("120.00"), new BigDecimal("3.00"),
                            new BigDecimal("360.00"), null, null));
            when(billItemMapper.insert(any(BillItem.class))).thenReturn(1);

            FeeItem fixed = new FeeItem();
            fixed.setId("f001");
            fixed.setName("Mgmt");
            fixed.setType("FIXED");

            FeeItem area = new FeeItem();
            area.setId("f002");
            area.setName("Area");
            area.setType("BY_AREA");

            Bill bill = billingEngine.generateBill(contract, List.of(fixed, area), "2026-04");
            assertEquals(0, new BigDecimal("560.00").compareTo(bill.getTotalAmount()));
            verify(billItemMapper, times(2)).insert(any(BillItem.class));
        }
    }

    @Nested @DisplayName("Edge Cases")
    class EdgeCases {

        @Test @DisplayName("empty fee list -> bill with zero total")
        void noFeeItems() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);

            Bill bill = billingEngine.generateBill(contract, Collections.emptyList(), "2026-05");
            assertEquals(0, BigDecimal.ZERO.compareTo(bill.getTotalAmount()));
            verify(billItemMapper, never()).insert(any(BillItem.class));
        }

        @Test @DisplayName("unknown strategy type -> skipped")
        void unknownStrategy() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);
            when(feeTierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
            when(strategies.get("UNKNOWN")).thenReturn(null);

            FeeItem fee = new FeeItem();
            fee.setId("f099");
            fee.setName("Mystery");
            fee.setType("UNKNOWN");

            Bill bill = billingEngine.generateBill(contract, List.of(fee), "2026-06");
            assertEquals(0, BigDecimal.ZERO.compareTo(bill.getTotalAmount()));
            verify(billItemMapper, never()).insert(any(BillItem.class));
        }

        @Test @DisplayName("bill no format is correct")
        void billNoFormat() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);

            Bill bill = billingEngine.generateBill(contract, Collections.emptyList(), "2026-07");
            assertTrue(bill.getNo().startsWith("BILL-202607-"));
            assertEquals("2026-07", bill.getPeriod());
        }

        @Test @DisplayName("bill due date is 15th of period month")
        void billDueDate() {
            when(unitMapper.selectById("u001")).thenReturn(unit);
            when(billMapper.insert(any(Bill.class))).thenReturn(1);
            when(billMapper.updateById(any(Bill.class))).thenReturn(1);

            Bill bill = billingEngine.generateBill(contract, Collections.emptyList(), "2026-08");
            assertEquals(java.time.LocalDate.of(2026, 8, 15), bill.getDueDate());
        }
    }
}
