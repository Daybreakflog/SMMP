package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_attachments")
public class TenantAttachment extends BaseEntity {
    private String tenantId;
    private String type;
    private String name;
    private String url;
    private Long size;
}
