package com.property.controller;

import com.property.service.ReportService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.property.dto.response.BillReportVO;
import com.property.dto.response.ComplaintReportVO;
import com.property.dto.response.WorkOrderReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "报表统计")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "工单统计（按状态分组 + 月趋势）")
    @GetMapping("/work-orders")
    @SaCheckPermission("report:view")
    public WorkOrderReportVO workOrders() {
        return reportService.workOrderReport();
    }

    @Operation(summary = "账单统计（收款总额 / 欠款总额 / 本月新增）")
    @GetMapping("/bills")
    @SaCheckPermission("report:view")
    public BillReportVO bills() {
        return reportService.billReport();
    }

    @Operation(summary = "投诉统计（按状态 / 类型分组）")
    @GetMapping("/complaints")
    @SaCheckPermission("report:view")
    public ComplaintReportVO complaints() {
        return reportService.complaintReport();
    }
}
