package com.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Token 信息")
public class TokenVO {

    @Schema(description = "访问令牌，请求受保护接口时放入 satoken header")
    private String accessToken;

    @Schema(description = "刷新令牌，accessToken 过期后用于换取新 token")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "accessToken 有效期（秒）", example = "86400")
    private long expiresIn = 86400;
}
