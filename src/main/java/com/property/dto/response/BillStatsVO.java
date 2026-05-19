package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillStatsVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal receivable;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal received;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal arrears;
}
