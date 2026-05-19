package com.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当前登录用户信息")
public class UserVO {

    @Schema(description = "用户 ID", example = "USR001")
    private String id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "姓名", example = "超级管理员")
    private String name;

    @Schema(description = "手机号", example = "13800000001")
    private String phone;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "账号状态：ACTIVE | INACTIVE | LOCKED", example = "ACTIVE")
    private String status;

    @Schema(description = "角色 code 列表", example = "[\"SuperAdmin\"]")
    private List<String> roles;

    @Schema(description = "权限码列表", example = "[\"tenant:view\",\"bill:edit\"]")
    private List<String> permissions;
}
