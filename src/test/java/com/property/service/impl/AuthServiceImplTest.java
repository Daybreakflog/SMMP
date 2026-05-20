package com.property.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.property.dto.request.LoginDTO;
import com.property.dto.request.RefreshDTO;
import com.property.dto.request.SmsLoginDTO;
import com.property.dto.response.LoginVO;
import com.property.dto.response.TokenVO;
import com.property.entity.User;
import com.property.exception.BusinessException;
import com.property.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock private UserService userService;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @InjectMocks private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId("u001");
        activeUser.setUsername("admin");
        activeUser.setPasswordHash(BCrypt.hashpw("123456"));
        activeUser.setName("Admin");
        activeUser.setPhone("13800000001");
        activeUser.setStatus("ACTIVE");
    }

    @Nested @DisplayName("Login")
    class LoginTests {

        @Test @DisplayName("correct password + ACTIVE -> LoginVO with tokens")
        void loginSuccess() {
            when(userService.findByUsername("admin")).thenReturn(activeUser);
            when(userService.findRolesByUserId("u001")).thenReturn(List.of("ADMIN"));
            when(userService.findPermissionsByUserId("u001")).thenReturn(List.of("user:read"));
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
                stp.when(() -> StpUtil.login("u001")).then(inv -> null);
                stp.when(StpUtil::getTokenValue).thenReturn("mock-access-token");

                LoginDTO dto = new LoginDTO();
                dto.setUsername("admin");
                dto.setPassword("123456");
                LoginVO result = authService.login(dto);

                assertNotNull(result.getToken());
                assertEquals("mock-access-token", result.getToken().getAccessToken());
                assertNotNull(result.getToken().getRefreshToken());
                assertTrue(result.getToken().getRefreshToken().startsWith("u001."));
                assertEquals("admin", result.getUser().getUsername());
                assertEquals(List.of("ADMIN"), result.getUser().getRoles());
            }
        }

        @Test @DisplayName("wrong password -> PASSWORD_WRONG")
        void wrongPassword() {
            when(userService.findByUsername("admin")).thenReturn(activeUser);

            LoginDTO dto = new LoginDTO();
            dto.setUsername("admin");
            dto.setPassword("wrong");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login(dto));
            assertEquals(40002, ex.getCode());
        }

        @Test @DisplayName("disabled user -> USER_DISABLED")
        void disabledUser() {
            activeUser.setStatus("LOCKED");
            when(userService.findByUsername("admin")).thenReturn(activeUser);

            LoginDTO dto = new LoginDTO();
            dto.setUsername("admin");
            dto.setPassword("123456");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login(dto));
            assertEquals(40003, ex.getCode());
        }
    }

    @Nested @DisplayName("SMS Login")
    class SmsLoginTests {
        @Test @DisplayName("active user by phone -> LoginVO")
        void smsLoginSuccess() {
            when(userService.findByPhone("13800000001")).thenReturn(activeUser);
            when(userService.findRolesByUserId("u001")).thenReturn(List.of("ADMIN"));
            when(userService.findPermissionsByUserId("u001")).thenReturn(List.of());
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
                stp.when(() -> StpUtil.login("u001")).then(inv -> null);
                stp.when(StpUtil::getTokenValue).thenReturn("sms-token");

                SmsLoginDTO dto = new SmsLoginDTO();
                dto.setPhone("13800000001");
                dto.setCode("123456");
                LoginVO result = authService.smsLogin(dto);

                assertEquals("sms-token", result.getToken().getAccessToken());
                assertEquals("u001", result.getUser().getId());
            }
        }
    }

    @Nested @DisplayName("Refresh")
    class RefreshTests {
        @Test @DisplayName("valid refresh token -> new TokenVO")
        void refreshOk() {
            String oldRefresh = "u001.old-uuid";
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refresh:u001")).thenReturn(oldRefresh);

            try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
                stp.when(() -> StpUtil.login("u001")).then(inv -> null);
                stp.when(StpUtil::getTokenValue).thenReturn("new-access-token");

                RefreshDTO dto = new RefreshDTO();
                dto.setRefreshToken(oldRefresh);
                TokenVO result = authService.refresh(dto);

                assertEquals("new-access-token", result.getAccessToken());
                assertNotNull(result.getRefreshToken());
                assertTrue(result.getRefreshToken().startsWith("u001."));
            }
        }

        @Test @DisplayName("mismatched refresh token -> TOKEN_INVALID")
        void refreshMismatch() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refresh:u001")).thenReturn("u001.correct-uuid");

            RefreshDTO dto = new RefreshDTO();
            dto.setRefreshToken("u001.wrong-uuid");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.refresh(dto));
            assertEquals(40101, ex.getCode());
        }

        @Test @DisplayName("malformed token (no dot) -> TOKEN_INVALID")
        void refreshMalformed() {
            RefreshDTO dto = new RefreshDTO();
            dto.setRefreshToken("nodottoken");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.refresh(dto));
            assertEquals(40101, ex.getCode());
        }
    }
}
