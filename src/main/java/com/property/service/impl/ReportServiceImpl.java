package com.property.service.impl;

import com.property.service.ReportService;

import com.property.mapper.BillMapper;
import com.property.mapper.ComplaintMapper;
import com.property.mapper.WorkOrderMapper;
import com.property.dto.response.BillReportVO;
import com.property.dto.response.ComplaintReportVO;
import com.property.dto.response.WorkOrderReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final WorkOrderMapper workOrderMapper;
    private final BillMapper billMapper;
    private final ComplaintMapper complaintMapper;

    public WorkOrderReportVO workOrderReport() {
        WorkOrderReportVO vo = new WorkOrderReportVO();
        vo.setByStatus(workOrderMapper.countByStatus());
        vo.setMonthly(workOrderMapper.countByMonth());
        return vo;
    }

    public BillReportVO billReport() {
        BillReportVO vo = new BillReportVO();
        vo.setTotalAmount(billMapper.sumTotalAmount());
        vo.setTotalArrears(billMapper.sumArrears());
        vo.setMonthlyNew(billMapper.countMonthlyNew());
        return vo;
    }

    public ComplaintReportVO complaintReport() {
        ComplaintReportVO vo = new ComplaintReportVO();
        vo.setByStatus(complaintMapper.countByStatus());
        vo.setByCategory(complaintMapper.countByCategory());
        return vo;
    }
}
