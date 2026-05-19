package com.property.service.strategy;

import com.property.dto.request.BillingContext;
import com.property.dto.response.CalcResult;
import com.property.entity.FeeItem;
import com.property.entity.FeeTier;
import com.property.entity.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("计费策略单元测试")
class BillingStrategyTest {

    // ── FixedStrategy ────────────────────────────────────────────

    @Nested
    @DisplayName("固定金额策略")
    class FixedTests {
        private final FixedStrategy strategy = new FixedStrategy();

        @Test
        @DisplayName("应返回 fixedAmount 作为金额")
        void shouldReturnFixedAmount() {
            FeeItem fee = new FeeItem();
            fee.setFixedAmount(new BigDecimal("100.00"));

            BillingContext ctx = new BillingContext(fee, null, null, null, null);
            CalcResult result = strategy.calculate(ctx);

            assertEquals(new BigDecimal("100.00"), result.amount());
            assertEquals(BigDecimal.ONE, result.quantity());
            assertEquals(new BigDecimal("100.00"), result.unitPrice());
            assertNull(result.meterStart());
            assertNull(result.meterEnd());
        }
    }

    // ── ByAreaStrategy ───────────────────────────────────────────

    @Nested
    @DisplayName("按面积策略")
    class ByAreaTests {
        private final ByAreaStrategy strategy = new ByAreaStrategy();

        @Test
        @DisplayName("金额 = 面积 × 单价")
        void shouldCalcAreaTimesUnitPrice() {
            FeeItem fee = new FeeItem();
            fee.setUnitPrice(new BigDecimal("3.50"));
            Unit unit = new Unit();
            unit.setArea(new BigDecimal("120.00"));

            BillingContext ctx = new BillingContext(fee, null, unit, null, null);
            CalcResult result = strategy.calculate(ctx);

            assertEquals(0, new BigDecimal("420.0000").compareTo(result.amount()));
            assertEquals(new BigDecimal("120.00"), result.quantity());
            assertEquals(new BigDecimal("3.50"), result.unitPrice());
        }

        @Test
        @DisplayName("面积为零时金额为零")
        void zeroAreaShouldGiveZero() {
            FeeItem fee = new FeeItem();
            fee.setUnitPrice(new BigDecimal("5.00"));
            Unit unit = new Unit();
            unit.setArea(BigDecimal.ZERO);

            BillingContext ctx = new BillingContext(fee, null, unit, null, null);
            CalcResult result = strategy.calculate(ctx);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.amount()));
        }
    }

    // ── ByMeterStrategy ──────────────────────────────────────────

    @Nested
    @DisplayName("按抄表策略")
    class ByMeterTests {
        private final ByMeterStrategy strategy = new ByMeterStrategy();

        @Test
        @DisplayName("金额 = (本次 - 上次) × 单价")
        void shouldCalcMeterDiffTimesPrice() {
            FeeItem fee = new FeeItem();
            fee.setUnitPrice(new BigDecimal("4.50"));

            BillingContext ctx = new BillingContext(
                    fee, null, null,
                    new BigDecimal("100"), new BigDecimal("250"));
            CalcResult result = strategy.calculate(ctx);

            assertEquals(0, new BigDecimal("675.0").compareTo(result.amount()));
            assertEquals(0, new BigDecimal("150").compareTo(result.quantity()));
            assertEquals(new BigDecimal("100"), result.meterStart());
            assertEquals(new BigDecimal("250"), result.meterEnd());
        }
    }

    // ── TieredStrategy ───────────────────────────────────────────

    @Nested
    @DisplayName("阶梯计费策略")
    class TieredTests {
        private final TieredStrategy strategy = new TieredStrategy();

        @Test
        @DisplayName("两级阶梯：0-100 按 2 元，100+ 按 3 元")
        void twoTierCalc() {
            FeeItem fee = new FeeItem();

            FeeTier t1 = new FeeTier();
            t1.setMinQty(BigDecimal.ZERO);
            t1.setMaxQty(new BigDecimal("100"));
            t1.setUnitPrice(new BigDecimal("2.00"));

            FeeTier t2 = new FeeTier();
            t2.setMinQty(new BigDecimal("100"));
            t2.setMaxQty(null); // 无上限
            t2.setUnitPrice(new BigDecimal("3.00"));

            BillingContext ctx = new BillingContext(
                    fee, List.of(t1, t2), null,
                    new BigDecimal("0"), new BigDecimal("150"));
            CalcResult result = strategy.calculate(ctx);

            // 100*2 + 50*3 = 200 + 150 = 350
            assertEquals(0, new BigDecimal("350.00").compareTo(result.amount()));
            assertEquals(0, new BigDecimal("150").compareTo(result.quantity()));
        }

        @Test
        @DisplayName("用量在第一阶梯内")
        void withinFirstTier() {
            FeeTier t1 = new FeeTier();
            t1.setMinQty(BigDecimal.ZERO);
            t1.setMaxQty(new BigDecimal("100"));
            t1.setUnitPrice(new BigDecimal("2.00"));

            FeeTier t2 = new FeeTier();
            t2.setMinQty(new BigDecimal("100"));
            t2.setMaxQty(null);
            t2.setUnitPrice(new BigDecimal("5.00"));

            BillingContext ctx = new BillingContext(
                    new FeeItem(), List.of(t1, t2), null,
                    new BigDecimal("0"), new BigDecimal("80"));
            CalcResult result = strategy.calculate(ctx);

            // 80*2 = 160
            assertEquals(0, new BigDecimal("160.00").compareTo(result.amount()));
        }

        @Test
        @DisplayName("零用量应返回零金额")
        void zeroUsage() {
            FeeTier t1 = new FeeTier();
            t1.setMinQty(BigDecimal.ZERO);
            t1.setMaxQty(new BigDecimal("100"));
            t1.setUnitPrice(new BigDecimal("2.00"));

            BillingContext ctx = new BillingContext(
                    new FeeItem(), List.of(t1), null,
                    new BigDecimal("50"), new BigDecimal("50"));
            CalcResult result = strategy.calculate(ctx);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.amount()));
        }
    }
}
