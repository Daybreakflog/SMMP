package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParkingSpaceDTO {

    @NotBlank(message = "车位编号不能为空")
    @Size(max = 30, message = "车位编号不超过30个字符")
    private String spaceNo;

    @NotBlank(message = "区域不能为空")
    @Size(max = 30, message = "区域不超过30个字符")
    private String zone;

    @Pattern(regexp = "AVAILABLE|OCCUPIED|MAINTENANCE", message = "状态只能是 AVAILABLE、OCCUPIED 或 MAINTENANCE")
    private String status;

    @Size(max = 500, message = "备注不超过500个字符")
    private String remark;
}
