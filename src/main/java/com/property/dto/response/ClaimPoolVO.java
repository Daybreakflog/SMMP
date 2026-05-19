package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClaimPoolVO {
    private String id;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal amount;
    private String channel;
    private String externalId;
    private String tenantId;
    private String status;
    private LocalDateTime receivedAt;
    private String remark;
}
