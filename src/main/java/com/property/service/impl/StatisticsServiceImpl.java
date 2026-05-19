package com.property.service.impl;

import com.property.service.StatisticsService;

import com.property.mapper.StatisticsMapper;
import com.property.dto.response.BillTrendVO;
import com.property.dto.response.OverviewVO;
import com.property.dto.response.WorkOrderTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public OverviewVO overview() {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        OverviewVO vo = new OverviewVO();
        vo.setWorkOrderTotal(statisticsMapper.countWorkOrderTotal(yearMonth));
        vo.setWorkOrderDone(statisticsMapper.countWorkOrderDone(yearMonth));
        vo.setWorkOrderOvertime(statisticsMapper.countWorkOrderOvertime(yearMonth));
        vo.setComplaintCount(statisticsMapper.countComplaint(yearMonth));
        vo.setBillTotalAmount(statisticsMapper.sumBillTotal(yearMonth));
        vo.setBillPaidAmount(statisticsMapper.sumBillPaid(yearMonth));
        vo.setAnnouncementCount(statisticsMapper.countAnnouncement(yearMonth));
        return vo;
    }

    public List<WorkOrderTrendVO> workOrderTrend(int days) {
        String startDate = LocalDate.now().minusDays(days - 1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        return statisticsMapper.selectWorkOrderTrend(startDate);
    }

    public List<BillTrendVO> billTrend(int months) {
        String startMonth = LocalDate.now().minusMonths(months - 1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return statisticsMapper.selectBillTrend(startMonth);
    }
}
