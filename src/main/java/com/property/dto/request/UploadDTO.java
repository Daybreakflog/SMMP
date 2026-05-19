package com.property.dto.request;

import lombok.Data;

@Data
public class UploadDTO {
    private String name;
    private Long size;
    private String mimeType;
    private String url;
    private String ossKey;
}
