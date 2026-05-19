package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("permissions")
public class Permission {
    /** 权限码作为主键，如 tenant:view */
    @TableId
    private String code;
    private String name;
    private String group;
}
