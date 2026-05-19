package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_configs")
public class SysConfig extends BaseEntity {
    @TableField("`key`")
    private String key;
    private String value;
    private String description;
}
