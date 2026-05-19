package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParkingSpaceAssignDTO {

    @NotBlank(message = "业主ID不能为空")
    private String ownerId;

    @NotBlank(message = "业主姓名不能为空")
    @Size(max = 64, message = "业主姓名不超过64个字符")
    private String ownerName;

    @NotBlank(message = "车牌号不能为空")
    @Size(max = 20, message = "车牌号不超过20个字符")
    private String vehiclePlate;
}
