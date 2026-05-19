package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_items")
public class SysDictItem extends BaseEntity {
    private String type;
    private String code;
    private String label;
    private Integer sort;
    private Boolean enabled;
}
