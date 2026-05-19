package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReconciliationVO {
    private String period;
    private String projectId;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal gatewayTotal;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal bankTotal;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal systemTotal;
    private int unmatchedCount;
    private List<DiffItem> diffs;

    @Data
    public static class DiffItem {
        private String externalId;
        private String channel;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal gatewayAmount;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal systemAmount;
        private String status;
    }
}
