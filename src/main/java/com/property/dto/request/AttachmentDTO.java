package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttachmentDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String url;
    private Long size;
}
