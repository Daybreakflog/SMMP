package com.property.controller;

import com.property.service.StatisticsService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.dto.response.BillTrendVO;
import com.property.dto.response.OverviewVO;
import com.property.dto.response.WorkOrderTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Statistics", description = "统计看板")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "数据概览（当月）")
    @GetMapping("/overview")
    @SaCheckPermission("stats:view")
    public OverviewVO overview() {
        return statisticsService.overview();
    }

    @Operation(summary = "工单趋势（按天）")
    @GetMapping("/workorder")
    @SaCheckPermission("stats:view")
    public List<WorkOrderTrendVO> workOrderTrend(
            @Parameter(description = "统计天数，默认30") @RequestParam(defaultValue = "30") int days) {
        return statisticsService.workOrderTrend(days);
    }

    @Operation(summary = "账单趋势（按月）")
    @GetMapping("/bill")
    @SaCheckPermission("stats:view")
    public List<BillTrendVO> billTrend(
            @Parameter(description = "统计月数，默认6") @RequestParam(defaultValue = "6") int months) {
        return statisticsService.billTrend(months);
    }
}
