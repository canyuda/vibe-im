package com.vibe.im.service;

import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.UserResponse;
import com.vibe.im.entity.User;
import com.vibe.im.entity.enums.UserStatus;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.repository.UserRepository;
import com.vibe.im.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService单元测试
 * 使用Mockito模拟依赖，测试各种场景下的业务逻辑
 *
 * <p>测试策略：
 * <ul>
 *   <li>使用@Nested分组组织测试用例</li>
 *   <li>使用@ParameterizedTest进行参数化测试</li>
 *   <li>验证Mock对象的调用次数和参数</li>
 *   <li>测试正常流程和异常流程</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("AuthService测试")
class AuthServiceTest {

    private UserRepository userRepository;
    private RedisTemplate<String, Object> redisTemplate;
    private SnowflakeIdGenerator snowflakeIdGenerator;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    // Mock操作对象
    private HashOperations<String, Object, Object> hashOperations;
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        // 创建Mock对象
        userRepository = mock(UserRepository.class);
        redisTemplate = mock(RedisTemplate.class);
        snowflakeIdGenerator = mock(SnowflakeIdGenerator.class);
        passwordEncoder = new BCryptPasswordEncoder(); // 使用真实实例

        // Mock Redis操作对象
        hashOperations = mock(HashOperations.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 创建待测试的服务实例
        authService = new AuthService(userRepository, redisTemplate, snowflakeIdGenerator, passwordEncoder);
    }

    /**
     * 用户注册测试套件
     */
    @Nested
    @DisplayName("用户注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功")
        void register_Success() {
            // 准备测试数据
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .nickname("测试用户")
                    .build();

            // Mock依赖行为
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            User savedUser = createMockUser(1L, "testuser", "测试用户", UserStatus.OFFLINE);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // 执行测试
            UserResponse response = authService.register(request);

            // 验证结果
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getNickname()).isEqualTo("测试用户");
            assertThat(response.getStatus()).isEqualTo(UserStatus.OFFLINE);

            // 验证Mock调用
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).save(argThat(user ->
                    "testuser".equals(user.getUsername()) &&
                            !user.getPassword().equals("password123") && // 密码已被加密
                            "测试用户".equals(user.getNickname()) &&
                            UserStatus.OFFLINE == user.getStatus()
            ));
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UserAlreadyExists() {
            // 准备测试数据
            RegisterRequest request = RegisterRequest.builder()
                    .username("existinguser")
                    .password("password123")
                    .nickname("已存在用户")
                    .build();

            // Mock依赖行为
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // 执行测试并验证异常
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_EXISTS);

            // 验证Mock调用
            verify(userRepository).existsByUsername("existinguser");
            verify(userRepository, never()).save(any());
        }

        @ParameterizedTest
        @CsvSource({
                "user1, pass123, 昵称1",
                "user2, pass456, 昵称2",
                "user3, pass789, 昵称3"
        })
        @DisplayName("注册成功 - 参数化测试")
        void register_Success_Parameterized(String username, String password, String nickname) {
            // 准备测试数据
            RegisterRequest request = RegisterRequest.builder()
                    .username(username)
                    .password(password)
                    .nickname(nickname)
                    .build();

            // Mock依赖行为
            when(userRepository.existsByUsername(username)).thenReturn(false);
            User savedUser = createMockUser(1L, username, nickname, UserStatus.OFFLINE);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // 执行测试
            UserResponse response = authService.register(request);

            // 验证结果
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(username);
            assertThat(response.getNickname()).isEqualTo(nickname);
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
        void login_Success() {
            // 准备测试数据
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            String encodedPassword = passwordEncoder.encode("password123");
            User mockUser = createMockUser(1L, "testuser", "测试用户", UserStatus.OFFLINE);
            mockUser.setPassword(encodedPassword);

            // Mock依赖行为
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
            when(redisTemplate.expire(anyString(), any())).thenReturn(true);

            // 执行测试
            LoginResponse response = authService.login(request);

            // 验证结果
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getSessionId()).isNotNull();
            assertThat(response.getStatus()).isEqualTo(UserStatus.ONLINE);

            // 验证Mock调用
            verify(userRepository).findByUsername("testuser");
            verify(redisTemplate.opsForHash()).put(anyString(), anyString(), anyString());
            verify(redisTemplate).expire(anyString(), any());
            verify(redisTemplate.opsForValue()).set(anyString(), anyString(), any());
            verify(userRepository).save(argThat(user -> UserStatus.ONLINE == user.getStatus()));
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void login_UserNotFound() {
            // 准备测试数据
            LoginRequest request = LoginRequest.builder()
                    .username("nonexistent")
                    .password("password123")
                    .build();

            // Mock依赖行为
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // 执行测试并验证异常
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_FAILED);

            // 验证Mock调用
            verify(userRepository).findByUsername("nonexistent");
            verify(redisTemplate, never()).opsForHash();
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void login_WrongPassword() {
            // 准备测试数据
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("wrongpassword")
                    .build();

            String encodedPassword = passwordEncoder.encode("correctpassword");
            User mockUser = createMockUser(1L, "testuser", "测试用户", UserStatus.OFFLINE);
            mockUser.setPassword(encodedPassword);

            // Mock依赖行为
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

            // 执行测试并验证异常
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_FAILED);

            // 验证Mock调用
            verify(userRepository).findByUsername("testuser");
            verify(redisTemplate, never()).opsForHash();
            verify(userRepository, never()).save(any());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCredentials")
        @DisplayName("登录失败 - 无效凭据参数化测试")
        void login_InvalidCredentials_Parameterized(String username, String password, boolean userExists) {
            // 准备测试数据
            LoginRequest request = LoginRequest.builder()
                    .username(username)
                    .password(password)
                    .build();

            // Mock依赖行为
            if (userExists) {
                String encodedPassword = passwordEncoder.encode("correctpassword");
                User mockUser = createMockUser(1L, username, "测试用户", UserStatus.OFFLINE);
                mockUser.setPassword(encodedPassword);
                when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            } else {
                when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
            }

            // 执行测试并验证异常
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_FAILED);
        }

        static Stream<org.junit.jupiter.params.provider.Arguments> provideInvalidCredentials() {
            return Stream.of(
                    org.junit.jupiter.params.provider.Arguments.of("user1", "wrongpass", true),
                    org.junit.jupiter.params.provider.Arguments.of("nonexistent", "anypass", false)
            );
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
        void logout_Success() {
            // 准备测试数据
            String sessionId = "test-session-id";
            Long userId = 1L;
            User mockUser = createMockUser(userId, "testuser", "测试用户", UserStatus.ONLINE);

            // Mock依赖行为
            when(hashOperations.get(anyString(), anyString())).thenReturn(userId.toString());
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            // 执行测试
            authService.logout(sessionId);

            // 验证Mock调用
            verify(hashOperations).get(anyString(), anyString());
            verify(redisTemplate, times(2)).delete(anyString());
            verify(userRepository).findById(userId);
            verify(userRepository).save(argThat(user -> UserStatus.OFFLINE == user.getStatus()));
        }

        @Test
        @DisplayName("登出 - 会话不存在")
        void logout_SessionNotFound() {
            // 准备测试数据
            String sessionId = "nonexistent-session-id";

            // Mock依赖行为
            when(hashOperations.get(anyString(), anyString())).thenReturn(null);

            // 执行测试（不应抛出异常）
            authService.logout(sessionId);

            // 验证Mock调用
            verify(hashOperations).get(anyString(), anyString());
            verify(redisTemplate, never()).delete(anyString());
            verify(userRepository, never()).findById(any());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "  "})
        @DisplayName("登出 - 无效会话ID")
        void logout_InvalidSessionId(String sessionId) {
            // 执行测试（不应抛出异常）
            authService.logout(sessionId);

            // 验证Mock调用
            if (sessionId == null || sessionId.trim().isEmpty()) {
                verify(hashOperations).get(anyString(), anyString());
            } else {
                verify(hashOperations).get(anyString(), anyString());
            }
            verify(redisTemplate, never()).delete(anyString());
        }
    }

    /**
     * 获取用户ID测试套件
     */
    @Nested
    @DisplayName("获取用户ID测试")
    class GetUserIdBySessionTests {

        @Test
        @DisplayName("获取用户ID成功")
        void getUserIdBySession_Success() {
            // 准备测试数据
            String sessionId = "test-session-id";
            Long userId = 1L;

            // Mock依赖行为
            when(hashOperations.get(anyString(), anyString())).thenReturn(userId.toString());
            when(redisTemplate.expire(anyString(), any())).thenReturn(true);

            // 执行测试
            Long result = authService.getUserIdBySession(sessionId);

            // 验证结果
            assertThat(result).isEqualTo(userId);

            // 验证Mock调用
            verify(hashOperations).get(anyString(), anyString());
            verify(redisTemplate).expire(anyString(), any());
        }

        @Test
        @DisplayName("获取用户ID - 会话不存在")
        void getUserIdBySession_SessionNotFound() {
            // 准备测试数据
            String sessionId = "nonexistent-session-id";

            // Mock依赖行为
            when(hashOperations.get(anyString(), anyString())).thenReturn(null);

            // 执行测试
            Long result = authService.getUserIdBySession(sessionId);

            // 验证结果
            assertThat(result).isNull();

            // 验证Mock调用
            verify(hashOperations).get(anyString(), anyString());
            verify(redisTemplate, never()).expire(anyString(), any());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "  ", "invalid-session"})
        @DisplayName("获取用户ID - 无效会话ID")
        void getUserIdBySession_InvalidSessionId(String sessionId) {
            // Mock依赖行为
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                when(hashOperations.get(anyString(), anyString())).thenReturn(null);
            }

            // 执行测试
            Long result = authService.getUserIdBySession(sessionId);

            // 验证结果
            assertThat(result).isNull();
        }
    }

    /**
     * 辅助方法：创建模拟用户对象
     *
     * @param id       用户ID
     * @param username 用户名
     * @param nickname 昵称
     * @param status   用户状态
     * @return 用户对象
     */
    private User createMockUser(Long id, String username, String nickname, UserStatus status) {
        return User.builder()
                .id(id)
                .username(username)
                .password("encodedPassword")
                .nickname(nickname)
                .avatar(null)
                .status(status)
                .createTime(LocalDateTime.now())
                .build();
    }
}
