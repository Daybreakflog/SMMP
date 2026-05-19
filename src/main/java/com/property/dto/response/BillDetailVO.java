package com.property.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BillDetailVO extends BillVO {

    private List<BillItemVO> items;
    private List<BillPaymentVO> payments;
    private List<BillLogVO> logs;

    @Data
    public static class BillItemVO {
        private String id;
        private String feeItemId;
        private String feeItemName;
        private String type;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal quantity;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal unitPrice;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal amount;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal meterStart;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal meterEnd;
    }

    @Data
    public static class BillPaymentVO {
        private String id;
        @JsonSerialize(using = ToStringSerializer.class)
        private BigDecimal amount;
        private String method;
        private String externalId;
        private LocalDateTime paidAt;
    }

    @Data
    public static class BillLogVO {
        private String id;
        private String action;
        private String content;
        private String operatorId;
        private LocalDateTime createdAt;
    }
}
