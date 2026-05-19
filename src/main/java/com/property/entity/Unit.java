package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("units")
public class Unit extends BaseEntity {
    private String no;
    private Integer floor;
    private BigDecimal area;
    private String type;
    private Integer roomCount;
    private String status;
    private String buildingId;
    private String projectId;
}
