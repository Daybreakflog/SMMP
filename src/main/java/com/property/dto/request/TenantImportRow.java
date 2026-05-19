package com.property.dto.request;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TenantImportRow {

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("姓名/公司名称")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("统一社会信用代码")
    private String socialCreditCode;

    @ExcelProperty("联系人")
    private String contactName;

    @ExcelProperty("联系人电话")
    private String contactPhone;

    @ExcelProperty("银行账号")
    private String bankAccount;
}
