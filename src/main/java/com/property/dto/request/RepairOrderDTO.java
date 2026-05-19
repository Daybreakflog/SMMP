package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairOrderDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String location;
}
