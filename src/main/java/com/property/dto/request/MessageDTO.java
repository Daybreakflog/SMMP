package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageDTO {
    @NotBlank
    private String content;
    private String senderId;
    /** TEXT | IMAGE | FILE */
    private String type = "TEXT";
}
