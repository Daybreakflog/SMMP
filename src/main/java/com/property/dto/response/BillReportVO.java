package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillReportVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal totalAmount;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal totalArrears;
    private Long monthlyNew;
}
