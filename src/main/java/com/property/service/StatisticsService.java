package com.property.service;

import com.property.dto.response.BillTrendVO;
import com.property.dto.response.OverviewVO;
import com.property.dto.response.WorkOrderTrendVO;
import java.util.List;

public interface StatisticsService {

    OverviewVO overview();

    List<WorkOrderTrendVO> workOrderTrend(int days);

    List<BillTrendVO> billTrend(int months);
}
