package com.property.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class WorkOrderReportVO {
    private List<StatusCountVO> byStatus;
    private List<MonthCountVO> monthly;
}
