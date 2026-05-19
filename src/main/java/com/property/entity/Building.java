package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("buildings")
public class Building extends BaseEntity {
    private String name;
    private Integer floorCount;
    private String projectId;
    private String status;
}
