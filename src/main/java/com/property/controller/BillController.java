package com.property.controller;

import com.property.service.BillService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.property.common.api.ApiEnvelope;
import com.property.common.api.PageResult;
import com.property.dto.request.BillDTO;
import com.property.dto.request.BillQueryDTO;
import com.property.dto.request.PaymentDTO;
import com.property.dto.request.PushDTO;
import com.property.dto.response.ArrearsVO;
import com.property.dto.response.BillDetailVO;
import com.property.dto.response.BillStatsVO;
import com.property.dto.response.BillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bill", description = "账单管理：收费项目 + 账单 + 收款")
@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @Operation(summary = "账单分页列表")
    @GetMapping
    @SaCheckPermission("bill:view")
    public PageResult<BillVO> list(@ModelAttribute BillQueryDTO query) {
        return billService.page(query);
    }

    @Operation(summary = "手动补单", description = "成功返回 HTTP 201")
    @PostMapping
    @SaCheckPermission("bill:edit")
    public ResponseEntity<ApiEnvelope<BillVO>> create(@Valid @RequestBody BillDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(billService.create(dto)));
    }

    @Operation(summary = "账单聚合统计")
    @GetMapping("/stats")
    @SaCheckPermission("bill:view")
    public BillStatsVO stats() {
        return billService.stats();
    }

    @Operation(summary = "欠费TOP列表")
    @GetMapping("/arrears")
    @SaCheckPermission("bill:view")
    public List<ArrearsVO> arrears(@RequestParam(defaultValue = "10") int topN) {
        return billService.arrears(topN);
    }

    @Operation(summary = "账单简版详情")
    @GetMapping("/{id}")
    @SaCheckPermission("bill:view")
    public BillVO get(@PathVariable String id) {
        return billService.getById(id);
    }

    @Operation(summary = "账单全量详情（含明细+收款+日志）")
    @GetMapping("/{id}/detail")
    @SaCheckPermission("bill:view")
    public BillDetailVO detail(@PathVariable String id) {
        return billService.getDetail(id);
    }

    @Operation(summary = "推送账单（发RabbitMQ）")
    @PostMapping("/push")
    @SaCheckPermission("bill:edit")
    public void push(@Valid @RequestBody PushDTO dto) {
        billService.push(dto);
    }

    @Operation(summary = "线下收款登记")
    @PostMapping("/{id}/payment")
    @SaCheckPermission("bill:collect")
    public BillVO collectPayment(@PathVariable String id, @Valid @RequestBody PaymentDTO dto) {
        return billService.collectPayment(id, dto, StpUtil.getLoginIdAsString());
    }

    @Operation(summary = "红冲账单")
    @PostMapping("/{id}/void")
    @SaCheckPermission("bill:edit")
    public BillVO voidBill(@PathVariable String id) {
        return billService.voidBill(id, StpUtil.getLoginIdAsString());
    }

    @Operation(summary = "小程序支付回写（无需登录）")
    @PostMapping("/{id}/pay")
    public BillVO payCallback(@PathVariable String id) {
        return billService.payCallback(id);
    }
}
