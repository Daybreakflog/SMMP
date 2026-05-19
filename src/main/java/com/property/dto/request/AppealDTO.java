package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppealDTO {
    @NotBlank
    private String reason;
    private String appealerId;
}
