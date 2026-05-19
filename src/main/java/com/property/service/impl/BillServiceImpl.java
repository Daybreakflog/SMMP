package com.property.service.impl;

import com.property.service.BillService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.*;
import com.property.constant.RabbitMQConstants;
import com.property.mapper.*;
import com.property.dto.request.BillDTO;
import com.property.dto.request.BillQueryDTO;
import com.property.dto.request.PaymentDTO;
import com.property.dto.request.PushDTO;
import com.property.dto.response.ArrearsVO;
import com.property.dto.response.BillDetailVO;
import com.property.dto.response.BillStatsVO;
import com.property.dto.response.BillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final BillPaymentMapper billPaymentMapper;
    private final BillLogMapper billLogMapper;
    private final PeriodCloseMapper periodCloseMapper;
    private final RabbitTemplate rabbitTemplate;

    public PageResult<BillVO> page(BillQueryDTO query) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<Bill>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Bill::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getPeriod()), Bill::getPeriod, query.getPeriod())
                .eq(StrUtil.isNotBlank(query.getTenantId()), Bill::getTenantId, query.getTenantId())
                .orderByDesc(Bill::getCreatedAt);
        return PageResult.of(billMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    @Transactional
    public BillVO create(BillDTO dto) {
        Bill bill = new Bill();
        bill.setNo("BILL-" + dto.getPeriod().replace("-", "") + "-M");
        bill.setPeriod(dto.getPeriod());
        bill.setContractId(dto.getContractId());
        bill.setTenantId(dto.getTenantId());
        bill.setUnitId(dto.getUnitId());
        bill.setProjectId(dto.getProjectId());
        bill.setTotalAmount(dto.getTotalAmount());
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus("UNPAID");
        bill.setDueDate(dto.getDueDate());
        billMapper.insert(bill);
        log(bill.getId(), "CREATE", "手动补单", null);
        return toVO(bill);
    }

    public BillStatsVO stats() {
        return billMapper.stats();
    }

    public List<ArrearsVO> arrears(int topN) {
        return billMapper.topArrears(topN > 0 ? topN : 10);
    }

    public BillVO getById(String id) {
        Bill bill = requireBill(id);
        return toVO(bill);
    }

    public BillDetailVO getDetail(String id) {
        Bill bill = requireBill(id);
        BillDetailVO vo = new BillDetailVO();
        copyToVO(bill, vo);

        vo.setItems(billItemMapper.selectList(
                        new LambdaQueryWrapper<BillItem>().eq(BillItem::getBillId, id))
                .stream().map(this::toItemVO).toList());

        vo.setPayments(billPaymentMapper.selectList(
                        new LambdaQueryWrapper<BillPayment>().eq(BillPayment::getBillId, id))
                .stream().map(this::toPaymentVO).toList());

        vo.setLogs(billLogMapper.selectList(
                        new LambdaQueryWrapper<BillLog>().eq(BillLog::getBillId, id)
                                .orderByAsc(BillLog::getCreatedAt))
                .stream().map(this::toLogVO).toList());

        return vo;
    }

    public void push(PushDTO dto) {
        for (String id : dto.getIds()) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_NOTIFICATION,
                    RabbitMQConstants.RK_NOTIFICATION_PUSH,
                    Map.of("type", "BILL_PUSH", "billId", id));
        }
    }

    @Transactional
    public BillVO collectPayment(String id, PaymentDTO dto, String operatorId) {
        Bill bill = requireBill(id);
        checkNotClosed(bill);
        if ("PAID".equals(bill.getStatus())) throw new BusinessException(ErrorCode.BILL_ALREADY_PAID);
        if ("VOID".equals(bill.getStatus())) throw new BusinessException(ErrorCode.STATUS_ILLEGAL);

        BillPayment payment = new BillPayment();
        payment.setBillId(id);
        payment.setAmount(dto.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setExternalId(dto.getExternalId());
        payment.setPaidAt(LocalDateTime.now());
        billPaymentMapper.insert(payment);

        BigDecimal newPaid = bill.getPaidAmount().add(dto.getAmount());
        bill.setPaidAmount(newPaid);
        if (newPaid.compareTo(bill.getTotalAmount()) >= 0) {
            bill.setStatus("PAID");
            bill.setPaidAt(LocalDateTime.now());
        } else {
            bill.setStatus("PARTIAL");
        }
        billMapper.updateById(bill);
        log(id, "PAYMENT", "线下收款 " + dto.getAmount(), operatorId);
        return toVO(bill);
    }

    @Transactional
    public BillVO voidBill(String id, String operatorId) {
        Bill bill = requireBill(id);
        checkNotClosed(bill);
        if ("PAID".equals(bill.getStatus())) throw new BusinessException(ErrorCode.BILL_ALREADY_PAID);
        bill.setStatus("VOID");
        bill.setVoidedAt(LocalDateTime.now());
        billMapper.updateById(bill);
        log(id, "VOID", "红冲", operatorId);
        return toVO(bill);
    }

    @Transactional
    public BillVO payCallback(String id) {
        Bill bill = requireBill(id);
        if ("PAID".equals(bill.getStatus())) return toVO(bill);
        bill.setStatus("PAID");
        bill.setPaidAt(LocalDateTime.now());
        bill.setPaidAmount(bill.getTotalAmount());
        billMapper.updateById(bill);
        log(id, "PAY_CALLBACK", "小程序支付回写", null);
        return toVO(bill);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Bill requireBill(String id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        return bill;
    }

    private void checkNotClosed(Bill bill) {
        long count = periodCloseMapper.selectCount(
                new LambdaQueryWrapper<PeriodClose>()
                        .eq(PeriodClose::getPeriod, bill.getPeriod())
                        .eq(PeriodClose::getProjectId, bill.getProjectId()));
        if (count > 0) throw new BusinessException(ErrorCode.BILL_ALREADY_CLOSED);
    }

    private void log(String billId, String action, String content, String operatorId) {
        BillLog entry = new BillLog();
        entry.setBillId(billId);
        entry.setAction(action);
        entry.setContent(content);
        entry.setOperatorId(operatorId);
        billLogMapper.insert(entry);
    }

    private BillVO toVO(Bill b) {
        BillVO vo = new BillVO();
        copyToVO(b, vo);
        return vo;
    }

    private void copyToVO(Bill b, BillVO vo) {
        vo.setId(b.getId());
        vo.setNo(b.getNo());
        vo.setPeriod(b.getPeriod());
        vo.setContractId(b.getContractId());
        vo.setTenantId(b.getTenantId());
        vo.setUnitId(b.getUnitId());
        vo.setProjectId(b.getProjectId());
        vo.setTotalAmount(b.getTotalAmount());
        vo.setPaidAmount(b.getPaidAmount());
        vo.setStatus(b.getStatus());
        vo.setDueDate(b.getDueDate());
        vo.setPaidAt(b.getPaidAt());
        vo.setVoidedAt(b.getVoidedAt());
        vo.setCreatedAt(b.getCreatedAt());
    }

    private BillDetailVO.BillItemVO toItemVO(BillItem i) {
        BillDetailVO.BillItemVO vo = new BillDetailVO.BillItemVO();
        vo.setId(i.getId());
        vo.setFeeItemId(i.getFeeItemId());
        vo.setFeeItemName(i.getFeeItemName());
        vo.setType(i.getType());
        vo.setQuantity(i.getQuantity());
        vo.setUnitPrice(i.getUnitPrice());
        vo.setAmount(i.getAmount());
        vo.setMeterStart(i.getMeterStart());
        vo.setMeterEnd(i.getMeterEnd());
        return vo;
    }

    private BillDetailVO.BillPaymentVO toPaymentVO(BillPayment p) {
        BillDetailVO.BillPaymentVO vo = new BillDetailVO.BillPaymentVO();
        vo.setId(p.getId());
        vo.setAmount(p.getAmount());
        vo.setMethod(p.getMethod());
        vo.setExternalId(p.getExternalId());
        vo.setPaidAt(p.getPaidAt());
        return vo;
    }

    private BillDetailVO.BillLogVO toLogVO(BillLog l) {
        BillDetailVO.BillLogVO vo = new BillDetailVO.BillLogVO();
        vo.setId(l.getId());
        vo.setAction(l.getAction());
        vo.setContent(l.getContent());
        vo.setOperatorId(l.getOperatorId());
        vo.setCreatedAt(l.getCreatedAt());
        return vo;
    }
}
