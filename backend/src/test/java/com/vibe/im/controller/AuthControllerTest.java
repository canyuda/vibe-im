package com.vibe.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.UserResponse;
import com.vibe.im.entity.enums.UserStatus;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController单元测试
 * 使用MockMvc测试REST端点，模拟AuthService行为
 *
 * <p>测试策略：
 * <ul>
 *   <li>使用@WebMvcTest加载Controller层</li>
 *   <li>使用@MockBean模拟AuthService</li>
 *   <li>测试正常流程和异常流程</li>
 *   <li>验证HTTP状态码和响应体</li>
 *   <li>测试请求参数校验</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("AuthController测试")
@WebMvcTest(controllers = AuthController.class)
@Import(com.vibe.im.exception.GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private UserResponse mockUserResponse;

    /**
     * 用户注册测试套件
     */
    @Nested
    @DisplayName("用户注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功")
        void register_Success() throws Exception {
            // 准备测试数据
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .nickname("测试用户")
                    .build();

            mockUserResponse = UserResponse.builder()
                    .id(1L)
                    .username("testuser")
                    .nickname("测试用户")
                    .status(UserStatus.OFFLINE)
                    .createTime(LocalDateTime.now())
                    .build();

            // Mock依赖行为
            when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserResponse);

            // 执行测试
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.nickname").value("测试用户"))
                    .andExpect(jsonPath("$.status").value("OFFLINE"));

            // 验证Mock调用
            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UserAlreadyExists() throws Exception {
            // 准备测试数据
            RegisterRequest request = RegisterRequest.builder()
                    .username("existinguser")
                    .password("password123")
                    .nickname("已存在用户")
                    .build();

            // Mock依赖行为
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.USER_ALREADY_EXISTS));

            // 执行测试
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(409))
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.message").value("用户已存在"));

            // 验证Mock调用
            verify(authService).register(any(RegisterRequest.class));
        }
    }

    /**
     * 用户登录测试套件
     */
    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void login_Success() throws Exception {
            // 准备测试数据
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            LoginResponse mockLoginResponse = LoginResponse.builder()
                    .id(1L)
                    .username("testuser")
                    .nickname("测试用户")
                    .status(UserStatus.ONLINE)
                    .createTime(LocalDateTime.now())
                    .sessionId("test-session-id")
                    .build();

            // Mock依赖行为
            when(authService.login(any(LoginRequest.class))).thenReturn(mockLoginResponse);

            // 执行测试
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.sessionId").value("test-session-id"))
                    .andExpect(jsonPath("$.status").value("ONLINE"));

            // 验证Mock调用
            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("登录失败 - 认证失败")
        void login_AuthenticationFailed() throws Exception {
            // 准备测试数据
            LoginRequest request = LoginRequest.builder()
                    .username("wronguser")
                    .password("wrongpassword")
                    .build();

            // Mock依赖行为
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTHENTICATION_FAILED));

            // 执行测试
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("认证失败"));

            // 验证Mock调用
            verify(authService).login(any(LoginRequest.class));
        }
    }

    /**
     * 用户登出测试套件
     */
    @Nested
    @DisplayName("用户登出测试")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void logout_Success() throws Exception {
            // 执行测试
            mockMvc.perform(post("/auth/logout")
                            .header("Session-Id", "test-session-id")
                            .header("User-Id", "1"))
                    .andExpect(status().isOk());

            // 验证Mock调用
            verify(authService).logout("test-session-id");
        }

        @Test
        @DisplayName("登出 - 不带请求头")
        void logout_WithoutHeaders() throws Exception {
            // 执行测试
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk());

            // 验证Mock调用
            verify(authService).logout(null);
        }
    }

    /**
     * 获取当前用户信息测试套件
     */
    @Nested
    @DisplayName("获取当前用户信息测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("获取当前用户成功")
        void getCurrentUser_Success() throws Exception {
            // Mock依赖行为
            when(authService.getUserIdBySession("test-session-id")).thenReturn(1L);

            // 执行测试
            mockMvc.perform(get("/auth/me")
                            .header("Session-Id", "test-session-id"))
                    .andExpect(status().isOk());

            // 验证Mock调用
            verify(authService).getUserIdBySession("test-session-id");
        }

        @Test
        @DisplayName("获取当前用户 - 会话无效")
        void getCurrentUser_SessionInvalid() throws Exception {
            // Mock依赖行为
            when(authService.getUserIdBySession("invalid-session")).thenReturn(null);

            // 执行测试
            mockMvc.perform(get("/auth/me")
                            .header("Session-Id", "invalid-session"))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("认证失败"));

            // 验证Mock调用
            verify(authService).getUserIdBySession("invalid-session");
        }

        @Test
        @DisplayName("获取当前用户 - 不带会话ID请求头")
        void getCurrentUser_NoSessionHeader() throws Exception {
            // 执行测试 - 由于Session-Id是必填的，应该返回400
            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));

            // 验证Mock调用 - 由于缺少请求头，service不应被调用
            verify(authService, never()).getUserIdBySession(anyString());
        }
    }
}
