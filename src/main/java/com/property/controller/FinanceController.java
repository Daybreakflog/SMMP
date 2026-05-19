package com.property.controller;

import com.property.service.FinanceService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.property.dto.request.AdjustDTO;
import com.property.dto.request.ClaimRulesDTO;
import com.property.dto.request.CloseDTO;
import com.property.dto.request.ManualClaimDTO;
import com.property.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Finance", description = "财务管理：对账 + 关账 + 流水认领")
@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @Operation(summary = "三方流水对账（mock）")
    @GetMapping("/reconciliation")
    @SaCheckPermission("finance:view")
    public ReconciliationVO reconciliation(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String projectId) {
        return financeService.reconciliation(period, projectId);
    }

    @Operation(summary = "差异调账")
    @PostMapping("/reconciliation/adjust")
    @SaCheckPermission("finance:edit")
    public void adjust(@Valid @RequestBody AdjustDTO dto) {
        financeService.adjust(dto);
    }

    @Operation(summary = "关账状态查询")
    @GetMapping("/close-status")
    @SaCheckPermission("finance:view")
    public CloseStatusVO closeStatus(
            @RequestParam String period,
            @RequestParam String projectId) {
        return financeService.closeStatus(period, projectId);
    }

    @Operation(summary = "执行关账", description = "confirm须='我已确认'，行锁+写快照")
    @PostMapping("/close")
    @SaCheckPermission("finance:close")
    public CloseStatusVO close(@Valid @RequestBody CloseDTO dto) {
        return financeService.close(dto, StpUtil.getLoginIdAsString());
    }

    @Operation(summary = "待认领流水列表")
    @GetMapping("/claim/pool")
    @SaCheckPermission("finance:view")
    public List<ClaimPoolVO> claimPool() {
        return financeService.claimPool();
    }

    @Operation(summary = "手动指派流水")
    @PostMapping("/claim/manual")
    @SaCheckPermission("finance:edit")
    public void manualClaim(@Valid @RequestBody ManualClaimDTO dto) {
        financeService.manualClaim(dto);
    }

    @Operation(summary = "规则列表")
    @GetMapping("/claim/rules")
    @SaCheckPermission("finance:view")
    public List<ClaimRuleVO> listRules(@RequestParam String projectId) {
        return financeService.listRules(projectId);
    }

    @Operation(summary = "覆盖保存规则（含priority排序）")
    @PutMapping("/claim/rules")
    @SaCheckPermission("finance:edit")
    public List<ClaimRuleVO> saveRules(@Valid @RequestBody ClaimRulesDTO dto) {
        return financeService.saveRules(dto);
    }
}
