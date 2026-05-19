package com.property.service;

import com.property.dto.response.BillReportVO;
import com.property.dto.response.ComplaintReportVO;
import com.property.dto.response.WorkOrderReportVO;

public interface ReportService {

    WorkOrderReportVO workOrderReport();

    BillReportVO billReport();

    ComplaintReportVO complaintReport();
}
