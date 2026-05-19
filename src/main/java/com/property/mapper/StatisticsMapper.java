package com.property.mapper;

import com.property.dto.response.BillTrendVO;
import com.property.dto.response.WorkOrderTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StatisticsMapper {

    Long countWorkOrderTotal(@Param("yearMonth") String yearMonth);

    Long countWorkOrderDone(@Param("yearMonth") String yearMonth);

    Long countWorkOrderOvertime(@Param("yearMonth") String yearMonth);

    Long countComplaint(@Param("yearMonth") String yearMonth);

    BigDecimal sumBillTotal(@Param("period") String period);

    BigDecimal sumBillPaid(@Param("period") String period);

    Long countAnnouncement(@Param("yearMonth") String yearMonth);

    List<WorkOrderTrendVO> selectWorkOrderTrend(@Param("startDate") String startDate);

    List<BillTrendVO> selectBillTrend(@Param("startMonth") String startMonth);
}
