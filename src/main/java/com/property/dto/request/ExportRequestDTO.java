package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ExportRequestDTO {
    @NotBlank
    private String type; // WORK_ORDER | BILL | TENANT
    private Map<String, Object> params;
}
