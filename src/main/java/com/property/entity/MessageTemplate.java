package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_templates")
public class MessageTemplate extends BaseEntity {
    private String code;
    /** SMS | EMAIL | PUSH | WECHAT */
    private String type;
    private String name;
    private String title;
    private String content;
    private String paramsJson;
    private Boolean enabled;
}
