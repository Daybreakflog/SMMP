package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FacilityDTO {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "ELEVATOR|FIRE_EQUIPMENT|WATER_SUPPLY|ELECTRICAL|OTHER",
            message = "类别只能是 ELEVATOR、FIRE_EQUIPMENT、WATER_SUPPLY、ELECTRICAL 或 OTHER")
    private String category;

    private String location;
    private LocalDate installDate;
    private String remark;
}
