package com.property.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.entity.Bill;
import com.property.entity.Contract;
import com.property.entity.FeeItem;
import com.property.entity.Unit;
import com.property.mapper.BillMapper;
import com.property.mapper.ContractMapper;
import com.property.mapper.FeeItemMapper;
import com.property.mapper.UnitMapper;
import com.property.service.impl.BillingEngine;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyBillingJob {

    private final ContractMapper contractMapper;
    private final FeeItemMapper feeItemMapper;
    private final BillMapper billMapper;
    private final UnitMapper unitMapper;
    private final BillingEngine billingEngine;

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @XxlJob("monthlyBillingJob")
    public void execute() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        String period = YearMonth.now().format(PERIOD_FMT);

        List<Contract> contracts = contractMapper.selectList(
                new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "ACTIVE"));

        int success = 0, skip = 0, fail = 0;

        for (int i = 0; i < contracts.size(); i++) {
            if (shardTotal > 1 && i % shardTotal != shardIndex) continue;

            Contract contract = contracts.get(i);
            try {
                // 幂等：同一账期+合同已生成则跳过
                long exists = billMapper.selectCount(
                        new LambdaQueryWrapper<Bill>()
                                .eq(Bill::getPeriod, period)
                                .eq(Bill::getContractId, contract.getId()));
                if (exists > 0) {
                    skip++;
                    continue;
                }

                Unit unit = unitMapper.selectById(contract.getUnitId());
                if (unit == null) {
                    log.warn("[monthlyBillingJob] 合同 {} 找不到房间，跳过", contract.getId());
                    skip++;
                    continue;
                }

                List<FeeItem> feeItems = feeItemMapper.selectList(
                        new LambdaQueryWrapper<FeeItem>()
                                .eq(FeeItem::getProjectId, unit.getProjectId())
                                .eq(FeeItem::getStatus, "ACTIVE"));

                billingEngine.generateBill(contract, feeItems, period);
                success++;
            } catch (Exception e) {
                fail++;
                log.error("[monthlyBillingJob] 合同 {} 生成失败: {}", contract.getId(), e.getMessage(), e);
            }
        }

        XxlJobHelper.log("period={} shard={}/{} success={} skip={} fail={}",
                period, shardIndex, shardTotal, success, skip, fail);
        XxlJobHelper.handleSuccess();
    }
}
