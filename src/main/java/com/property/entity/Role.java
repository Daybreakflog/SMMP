package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("roles")
public class Role {
    /** 角色码作为主键，如 SuperAdmin */
    @TableId
    private String code;
    private String name;
    private String description;
    private Boolean builtIn;
}
