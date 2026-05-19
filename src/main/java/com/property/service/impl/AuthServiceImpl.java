package com.property.service.impl;

import com.property.service.AuthService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.User;
import com.property.dto.request.LoginDTO;
import com.property.dto.request.RefreshDTO;
import com.property.dto.request.SmsLoginDTO;
import com.property.dto.response.LoginVO;
import com.property.dto.response.TokenVO;
import com.property.dto.response.UserVO;
import com.property.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_KEY_PREFIX = "refresh:";
    private static final long REFRESH_TTL_DAYS = 30;

    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    public LoginVO login(LoginDTO dto) {
        User user = userService.findByUsername(dto.getUsername());
        if (!BCrypt.checkpw(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_WRONG);
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return buildLoginResult(user);
    }

    public LoginVO smsLogin(SmsLoginDTO dto) {
        // SMS code 校验交由短信服务（本里程碑为占位实现，不验证 code）
        User user = userService.findByPhone(dto.getPhone());
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return buildLoginResult(user);
    }

    public TokenVO refresh(RefreshDTO dto) {
        String incoming = dto.getRefreshToken();
        // refreshToken 格式：{userId}.{uuid}
        int dotIdx = incoming.indexOf('.');
        if (dotIdx < 1) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        String userId = incoming.substring(0, dotIdx);
        String stored = stringRedisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + userId);
        if (!incoming.equals(stored)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        // 颁发新 Sa-Token session
        StpUtil.login(userId);
        String newAccessToken = StpUtil.getTokenValue();
        // 轮换 refresh token（旧 key 覆盖写入）
        String newRefreshToken = storeRefreshToken(userId);
        TokenVO token = new TokenVO();
        token.setAccessToken(newAccessToken);
        token.setRefreshToken(newRefreshToken);
        return token;
    }

    public void logout() {
        String userId = StpUtil.getLoginIdAsString();
        StpUtil.logout();
        stringRedisTemplate.delete(REFRESH_KEY_PREFIX + userId);
    }

    public UserVO me() {
        String userId = StpUtil.getLoginIdAsString();
        User user = userService.findById(userId);
        return buildUserVO(user);
    }

    // ── 内部 ────────────────────────────────────────────────────────────────

    private LoginVO buildLoginResult(User user) {
        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();
        String refreshToken = storeRefreshToken(user.getId());

        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(accessToken);
        tokenVO.setRefreshToken(refreshToken);

        LoginVO result = new LoginVO();
        result.setToken(tokenVO);
        result.setUser(buildUserVO(user));
        return result;
    }

    /**
     * 生成并写入 refresh token，key=refresh:{userId}，TTL 30天。
     * 格式：{userId}.{UUID}，允许 refresh 时从 token 中提取 userId。
     */
    private String storeRefreshToken(String userId) {
        String token = userId + "." + UUID.randomUUID();
        stringRedisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + userId, token, REFRESH_TTL_DAYS, TimeUnit.DAYS);
        return token;
    }

    private UserVO buildUserVO(User user) {
        List<String> roles = userService.findRolesByUserId(user.getId());
        List<String> permissions = userService.findPermissionsByUserId(user.getId());

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        return vo;
    }
}
