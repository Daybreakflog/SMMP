package com.property.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "租户详情")
public class TenantVO {

    @Schema(description = "租户ID")
    private String id;

    @Schema(description = "租户类型", allowableValues = {"PERSONAL", "COMPANY"})
    private String type;

    @Schema(description = "姓名（个人）或公司名称")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "身份证号（个人）")
    private String idCard;

    @Schema(description = "统一社会信用代码（企业）")
    private String socialCreditCode;

    @Schema(description = "联系人姓名（企业）")
    private String contactName;

    @Schema(description = "联系人电话（企业）")
    private String contactPhone;

    @Schema(description = "银行账号")
    private String bankAccount;

    @Schema(description = "状态", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "创建时间（ISO 8601）")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "更新时间（ISO 8601）")
    private LocalDateTime updatedAt;
}
