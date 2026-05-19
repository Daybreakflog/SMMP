package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ArrearsVO {
    private String tenantId;
    private String tenantName;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal arrears;
}
