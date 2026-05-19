package com.property.controller;

import com.property.service.AuthService;

import com.property.dto.request.LoginDTO;
import com.property.dto.request.RefreshDTO;
import com.property.dto.request.SmsLoginDTO;
import com.property.dto.response.LoginVO;
import com.property.dto.response.TokenVO;
import com.property.dto.response.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "鉴权：登录 / 刷新 / 注销")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "账号密码登录", description = "返回 accessToken + refreshToken + 用户信息（含角色和权限列表）")
    @PostMapping("/login")
    public LoginVO login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms-login")
    public LoginVO smsLogin(@Valid @RequestBody SmsLoginDTO dto) {
        return authService.smsLogin(dto);
    }

    @Operation(summary = "获取当前登录用户信息", description = "需要在 Header 中携带 satoken")
    @GetMapping("/me")
    public UserVO me() {
        return authService.me();
    }

    @Operation(summary = "刷新 accessToken", description = "使用 refreshToken 换取新的 accessToken，refresh token 同步轮换")
    @PostMapping("/refresh")
    public TokenVO refresh(@Valid @RequestBody RefreshDTO dto) {
        return authService.refresh(dto);
    }

    @Operation(summary = "退出登录", description = "注销当前 session 并删除 refresh token 白名单")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "获取图形验证码（占位）", description = "暂未接入真实图形验证码服务，返回 mock 数据")
    @GetMapping("/captcha")
    public Map<String, String> captcha() {
        return Map.of(
                "captchaKey", "mock-key-" + System.currentTimeMillis(),
                "captchaImg", "data:image/png;base64,mock"
        );
    }

    @Operation(summary = "发送短信验证码（占位）", description = "暂未接入真实短信服务，始终返回成功")
    @PostMapping("/sms/send")
    public Map<String, String> sendSms(@RequestBody Map<String, String> body) {
        return Map.of("message", "验证码已发送，请注意查收");
    }
}
