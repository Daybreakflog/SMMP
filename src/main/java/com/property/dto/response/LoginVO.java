package com.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "Token 信息")
    private TokenVO token;

    @Schema(description = "登录用户信息（含角色和权限）")
    private UserVO user;
}
