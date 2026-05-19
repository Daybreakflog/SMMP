package com.property.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "租户请求体")
public class TenantDTO {

    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "PERSONAL|COMPANY", message = "类型只能是 PERSONAL 或 COMPANY")
    @Schema(description = "租户类型", example = "PERSONAL", allowableValues = {"PERSONAL", "COMPANY"})
    private String type;

    @NotBlank(message = "姓名/公司名称不能为空")
    @Size(max = 64, message = "姓名/公司名称不超过64个字符")
    @Schema(description = "姓名（个人）或公司名称", example = "张三")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号必须为11位数字")
    @Schema(description = "手机号", example = "13812345678")
    private String phone;

    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号必须为18位")
    @Schema(description = "身份证号（个人租户）", example = "110101199001011234")
    private String idCard;

    @Pattern(regexp = "^[0-9A-HJ-NP-RT-UW-Y]{18}$", message = "统一社会信用代码必须为18位")
    @Schema(description = "统一社会信用代码（企业租户）", example = "91110000717344498L")
    private String socialCreditCode;

    @Size(max = 32, message = "联系人姓名不超过32个字符")
    @Schema(description = "联系人姓名（企业租户）")
    private String contactName;

    @Pattern(regexp = "^\\d{11}$|^$", message = "联系人电话必须为11位数字")
    @Schema(description = "联系人电话（企业租户）")
    private String contactPhone;

    @Size(max = 64, message = "银行账号不超过64个字符")
    @Schema(description = "银行账号")
    private String bankAccount;
}
