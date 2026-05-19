package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class SendNotificationDTO {
    @NotBlank
    @Pattern(regexp = "USER|ROLE|ALL")
    private String targetType;

    private String targetId;

    private String templateCode;
    private String channel = "IN_APP";
    private String type;
    private String title;
    private String content;
    private Map<String, Object> params;
}
