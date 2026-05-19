package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OverviewVO {
    private Long workOrderTotal;
    private Long workOrderDone;
    private Long workOrderOvertime;
    private Long complaintCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal billTotalAmount;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal billPaidAmount;
    private Long announcementCount;
}
