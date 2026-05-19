package com.property.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租户列表查询参数")
public class TenantQueryDTO {

    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    private int page = 1;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private int pageSize = 10;

    @Schema(description = "关键字（匹配姓名/公司名称、手机号）", example = "张三")
    private String keyword;

    @Schema(description = "租户类型", example = "PERSONAL", allowableValues = {"PERSONAL", "COMPANY"})
    private String type;
}
