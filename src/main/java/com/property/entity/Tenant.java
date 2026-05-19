package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenants")
public class Tenant extends BaseEntity {
    private String type;
    private String name;
    private String idCard;
    private String phone;
    private String socialCreditCode;
    private String contactName;
    private String contactPhone;
    private String bankAccount;
    private String status;
}
