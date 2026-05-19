package com.property.service.impl;

import com.property.service.FinanceService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.*;
import com.property.mapper.*;
import com.property.dto.request.AdjustDTO;
import com.property.dto.request.ClaimRulesDTO;
import com.property.dto.request.CloseDTO;
import com.property.dto.request.ManualClaimDTO;
import com.property.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final PeriodCloseMapper periodCloseMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final ClaimMapper claimMapper;
    private final ClaimRuleMapper claimRuleMapper;

    // ── reconciliation ────────────────────────────────────────────────────────

    public ReconciliationVO reconciliation(String period, String projectId) {
        // Mock gateway + bank data for this stage (B7 will connect real gateways)
        List<MockTxn> gatewayData = mockGatewayData();
        List<MockTxn> bankData = mockBankData();

        List<PaymentRecord> systemRecords = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .isNotNull(PaymentRecord::getExternalId));

        Map<String, PaymentRecord> systemMap = systemRecords.stream()
                .collect(Collectors.toMap(PaymentRecord::getExternalId, Function.identity(),
                        (a, b) -> a));

        List<ReconciliationVO.DiffItem> diffs = new ArrayList<>();
        BigDecimal gatewayTotal = BigDecimal.ZERO;

        for (MockTxn txn : gatewayData) {
            gatewayTotal = gatewayTotal.add(txn.amount());
            PaymentRecord pr = systemMap.get(txn.externalId());
            if (pr == null || pr.getAmount().compareTo(txn.amount()) != 0) {
                ReconciliationVO.DiffItem diff = new ReconciliationVO.DiffItem();
                diff.setExternalId(txn.externalId());
                diff.setChannel(txn.channel());
                diff.setGatewayAmount(txn.amount());
                diff.setSystemAmount(pr != null ? pr.getAmount() : null);
                diff.setStatus(pr == null ? "MISSING" : "MISMATCH");
                diffs.add(diff);
            }
        }

        BigDecimal systemTotal = systemRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReconciliationVO vo = new ReconciliationVO();
        vo.setPeriod(period);
        vo.setProjectId(projectId);
        vo.setGatewayTotal(gatewayTotal);
        vo.setBankTotal(bankData.stream().map(MockTxn::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setSystemTotal(systemTotal);
        vo.setUnmatchedCount(diffs.size());
        vo.setDiffs(diffs);
        return vo;
    }

    @Transactional
    public void adjust(AdjustDTO dto) {
        Claim claim = new Claim();
        claim.setPaymentRecordId(dto.getPaymentRecordId());
        claim.setBillId(dto.getBillId());
        claim.setAmount(dto.getAmount());
        claim.setAutoClaimed(false);
        claimMapper.insert(claim);

        PaymentRecord pr = paymentRecordMapper.selectById(dto.getPaymentRecordId());
        if (pr != null) {
            pr.setStatus("MATCHED");
            pr.setReconciledAt(LocalDateTime.now());
            paymentRecordMapper.updateById(pr);
        }
    }

    // ── close ────────────────────────────────────────────────────────────────

    public CloseStatusVO closeStatus(String period, String projectId) {
        PeriodClose close = periodCloseMapper.selectOne(
                new LambdaQueryWrapper<PeriodClose>()
                        .eq(PeriodClose::getPeriod, period)
                        .eq(PeriodClose::getProjectId, projectId)
                        .last("LIMIT 1"));

        CloseStatusVO vo = new CloseStatusVO();
        vo.setPeriod(period);
        vo.setProjectId(projectId);
        if (close != null) {
            vo.setClosed(true);
            vo.setClosedAt(close.getClosedAt());
            vo.setBy(close.getClosedBy());
        } else {
            vo.setClosed(false);
        }
        return vo;
    }

    @Transactional
    public CloseStatusVO close(CloseDTO dto, String operatorId) {
        if (!"我已确认".equals(dto.getConfirm())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // Row lock: prevent concurrent close for same period+project
        periodCloseMapper.selectList(
                new LambdaQueryWrapper<PeriodClose>()
                        .eq(PeriodClose::getPeriod, dto.getPeriod())
                        .eq(PeriodClose::getProjectId, dto.getProjectId())
                        .last("FOR UPDATE"));

        long already = periodCloseMapper.selectCount(
                new LambdaQueryWrapper<PeriodClose>()
                        .eq(PeriodClose::getPeriod, dto.getPeriod())
                        .eq(PeriodClose::getProjectId, dto.getProjectId()));
        if (already > 0) {
            return closeStatus(dto.getPeriod(), dto.getProjectId());
        }

        PeriodClose close = new PeriodClose();
        close.setPeriod(dto.getPeriod());
        close.setProjectId(dto.getProjectId());
        close.setClosedBy(operatorId);
        close.setClosedAt(LocalDateTime.now());
        close.setNotes(dto.getNotes());
        close.setTotalRevenue(BigDecimal.ZERO);
        periodCloseMapper.insert(close);

        return closeStatus(dto.getPeriod(), dto.getProjectId());
    }

    // ── claim ─────────────────────────────────────────────────────────────────

    public List<ClaimPoolVO> claimPool() {
        return paymentRecordMapper.selectList(
                        new LambdaQueryWrapper<PaymentRecord>()
                                .in(PaymentRecord::getStatus, "PENDING", "UNMATCHED")
                                .orderByDesc(PaymentRecord::getReceivedAt))
                .stream().map(this::toClaimPoolVO).toList();
    }

    @Transactional
    public void manualClaim(ManualClaimDTO dto) {
        for (ManualClaimDTO.Allocation alloc : dto.getAllocations()) {
            Claim claim = new Claim();
            claim.setPaymentRecordId(dto.getPaymentId());
            claim.setBillId(alloc.getBillId());
            claim.setAmount(alloc.getAmount());
            claim.setAutoClaimed(false);
            claimMapper.insert(claim);
        }
        PaymentRecord pr = paymentRecordMapper.selectById(dto.getPaymentId());
        if (pr != null) {
            pr.setStatus("MATCHED");
            pr.setClaimedAt(LocalDateTime.now());
            paymentRecordMapper.updateById(pr);
        }
    }

    public List<ClaimRuleVO> listRules(String projectId) {
        return claimRuleMapper.selectList(
                        new LambdaQueryWrapper<ClaimRule>()
                                .eq(ClaimRule::getProjectId, projectId)
                                .orderByAsc(ClaimRule::getPriority))
                .stream().map(this::toRuleVO).toList();
    }

    @Transactional
    public List<ClaimRuleVO> saveRules(ClaimRulesDTO dto) {
        claimRuleMapper.delete(
                new LambdaQueryWrapper<ClaimRule>()
                        .eq(ClaimRule::getProjectId, dto.getProjectId()));

        List<ClaimRule> rules = new ArrayList<>();
        for (ClaimRulesDTO.RuleItem item : dto.getRules()) {
            ClaimRule rule = new ClaimRule();
            rule.setName(item.getName());
            rule.setPriority(item.getPriority());
            rule.setConditionJson(item.getConditionJson());
            rule.setActionJson(item.getActionJson());
            rule.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
            rule.setProjectId(dto.getProjectId());
            claimRuleMapper.insert(rule);
            rules.add(rule);
        }
        return rules.stream().map(this::toRuleVO).toList();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ClaimPoolVO toClaimPoolVO(PaymentRecord pr) {
        ClaimPoolVO vo = new ClaimPoolVO();
        vo.setId(pr.getId());
        vo.setAmount(pr.getAmount());
        vo.setChannel(pr.getChannel());
        vo.setExternalId(pr.getExternalId());
        vo.setTenantId(pr.getTenantId());
        vo.setStatus(pr.getStatus());
        vo.setReceivedAt(pr.getReceivedAt());
        vo.setRemark(pr.getRemark());
        return vo;
    }

    private ClaimRuleVO toRuleVO(ClaimRule r) {
        ClaimRuleVO vo = new ClaimRuleVO();
        vo.setId(r.getId());
        vo.setName(r.getName());
        vo.setPriority(r.getPriority());
        vo.setConditionJson(r.getConditionJson());
        vo.setActionJson(r.getActionJson());
        vo.setEnabled(r.getEnabled());
        vo.setProjectId(r.getProjectId());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    // ── mock data (B7 替换为真实接入) ─────────────────────────────────────────

    private record MockTxn(String externalId, String channel, BigDecimal amount) {}

    private List<MockTxn> mockGatewayData() {
        return List.of(
                new MockTxn("GW-2026050001", "ALIPAY", new BigDecimal("1200.00")),
                new MockTxn("GW-2026050002", "WECHAT", new BigDecimal("800.00")),
                new MockTxn("GW-2026050003", "ALIPAY", new BigDecimal("2000.00"))
        );
    }

    private List<MockTxn> mockBankData() {
        return List.of(
                new MockTxn("BK-2026050001", "BANK", new BigDecimal("1200.00")),
                new MockTxn("BK-2026050002", "BANK", new BigDecimal("800.00"))
        );
    }
}
