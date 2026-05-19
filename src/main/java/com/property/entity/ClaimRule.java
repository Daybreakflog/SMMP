package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("claim_rules")
public class ClaimRule extends BaseEntity {
    private String name;
    private Integer priority;
    private String conditionJson;
    private String actionJson;
    private Boolean enabled;
    private String projectId;
}
