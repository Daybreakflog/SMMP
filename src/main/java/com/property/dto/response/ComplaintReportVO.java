package com.property.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ComplaintReportVO {
    private List<StatusCountVO> byStatus;
    private List<CategoryCountVO> byCategory;
}
