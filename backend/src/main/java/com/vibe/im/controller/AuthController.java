package com.vibe.im.controller;

import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.UserResponse;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册、登录、登出和当前用户信息查询的REST API
 *
 * <p>端点说明：
 * <ul>
 *   <li>POST /auth/register - 用户注册</li>
 *   <li>POST /auth/login - 用户登录</li>
 *   <li>POST /auth/logout - 用户登出</li>
 *   <li>GET /auth/me - 获取当前用户信息</li>
 * </ul>
 *
 * <p>会话管理：
 * <ul>
 *   <li>登录成功后返回sessionId</li>
 *   <li>后续请求需在请求头中携带Session-Id</li>
 *   <li>登出时可选传递Session-Id和User-Id请求头</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * <p>请求示例：
     * <pre>
     * POST /auth/register
     * Content-Type: application/json
     * {
     *   "username": "testuser",
     *   "password": "password123",
     *   "nickname": "测试用户"
     * }
     * </pre>
     *
     * <p>响应示例（200 OK）：
     * <pre>
     * {
     *   "id": 1,
     *   "username": "testuser",
     *   "nickname": "测试用户",
     *   "avatar": null,
     *   "status": "OFFLINE",
     *   "createTime": "2026-03-30T10:00:00"
     * }
     * </pre>
     *
     * @param request 注册请求
     * @return 用户信息
     * @throws BusinessException 当用户名已存在时抛出
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求，用户名: {}", request.getUsername());
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     *
     * <p>请求示例：
     * <pre>
     * POST /auth/login
     * Content-Type: application/json
     * {
     *   "username": "testuser",
     *   "password": "password123"
     * }
     * </pre>
     *
     * <p>响应示例（200 OK）：
     * <pre>
     * {
     *   "id": 1,
     *   "username": "testuser",
     *   "nickname": "测试用户",
     *   "avatar": null,
     *   "status": "ONLINE",
     *   "createTime": "2026-03-30T10:00:00",
     *   "sessionId": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * </pre>
     *
     * @param request 登录请求
     * @return 登录响应（包含sessionId）
     * @throws BusinessException 当认证失败时抛出
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求，用户名: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出
     *
     * <p>请求示例：
     * <pre>
     * POST /auth/logout
     * Session-Id: 550e8400-e29b-41d4-a716-446655440000
     * User-Id: 1
     * </pre>
     *
     * <p>响应示例（200 OK）：空响应体
     *
     * <p>说明：Session-Id和User-Id为可选参数，用于辅助登出操作
     *
     * @param sessionId 会话ID（可选）
     * @param userId 用户ID（可选）
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Session-Id", required = false) String sessionId,
            @RequestHeader(value = "User-Id", required = false) String userId) {
        log.info("收到登出请求，会话ID: {}, 用户ID: {}", sessionId, userId);
        authService.logout(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取当前用户信息
     *
     * <p>请求示例：
     * <pre>
     * GET /auth/me
     * Session-Id: 550e8400-e29b-41d4-a716-446655440000
     * </pre>
     *
     * <p>响应示例（200 OK）：
     * <pre>
     * {
     *   "id": 1,
     *   "username": "testuser",
     *   "nickname": "测试用户",
     *   "avatar": null,
     *   "status": "ONLINE",
     *   "createTime": "2026-03-30T10:00:00"
     * }
     * </pre>
     *
     * <p>响应示例（401 Unauthorized）：
     * <pre>
     * {
     *   "code": 401,
     *   "message": "认证失败",
     *   "timestamp": "2026-03-30T10:00:00Z"
     * }
     * </pre>
     *
     * @param sessionId 会话ID（必填）
     * @return 用户信息，如果用户不存在返回204 No Content
     * @throws BusinessException 当会话无效时抛出
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader(value = "Session-Id") String sessionId) {
        log.debug("收到获取当前用户请求，会话ID: {}", sessionId);

        // 根据sessionId获取userId
        Long userId = authService.getUserIdBySession(sessionId);
        if (userId == null) {
            log.warn("会话验证失败，会话ID: {}", sessionId);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // TODO: 需要UserService来获取完整的用户信息
        // 目前暂时返回204 No Content，待UserService实现后完善
        log.info("用户信息获取成功，用户ID: {}", userId);
        return ResponseEntity.ok().build();
    }
}
