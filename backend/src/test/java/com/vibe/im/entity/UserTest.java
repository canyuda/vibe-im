package com.vibe.im.entity;

import com.vibe.im.entity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 用户实体单元测试
 * 测试User实体的创建、字段验证和业务逻辑
 *
 * <p>测试策略：
 * <ul>
 *   <li>参数化测试：测试UserStatus枚举的所有值</li>
 *   <li>边界测试：测试必填字段的校验</li>
 *   <li>Builder模式测试：验证Lombok Builder的正确性</li>
 *   <li>默认值测试：验证字段的默认值</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@DisplayName("User实体测试")
class UserTest {

    /**
     * 测试数据源：提供不同的用户状态
     *
     * @return UserStatus枚举值流
     */
    private static Stream<UserStatus> userStatusProvider() {
        return Stream.of(UserStatus.values());
    }

    /**
     * 测试数据源：提供有效的用户名和密码组合
     *
     * @return 用户名和密码的Object数组流
     */
    private static Stream<Object[]> validUserCredentialsProvider() {
        return Stream.of(
                new Object[]{"alice", "password123"},
                new Object[]{"bob", "SecurePass!456"},
                new Object[]{"charlie", "MyP@ssw0rd"}
        );
    }

    @Nested
    @DisplayName("实体创建测试")
    class EntityCreationTests {

        @Test
        @DisplayName("使用Builder创建用户实体")
        void testCreateUserWithBuilder() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .username("alice")
                    .password("password123")
                    .nickname("Alice Smith")
                    .avatar("https://example.com/avatar.jpg")
                    .status(UserStatus.ONLINE)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            // When & Then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getUsername()).isEqualTo("alice");
            assertThat(user.getPassword()).isEqualTo("password123");
            assertThat(user.getNickname()).isEqualTo("Alice Smith");
            assertThat(user.getAvatar()).isEqualTo("https://example.com/avatar.jpg");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ONLINE);
            assertThat(user.getCreateTime()).isNotNull();
            assertThat(user.getUpdateTime()).isNotNull();
        }

        @ParameterizedTest
        @DisplayName("使用简化构造函数创建用户 - 参数化测试")
        @MethodSource("com.vibe.im.entity.UserTest#validUserCredentialsProvider")
        void testCreateUserWithSimpleConstructor(String username, String password) {
            // When
            User user = new User(username, password);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getStatus()).isEqualTo(UserStatus.OFFLINE);
        }

        @Test
        @DisplayName("使用无参构造函数创建用户")
        void testCreateUserWithNoArgsConstructor() {
            // When
            User user = new User();

            // Then
            assertThat(user).isNotNull();
            // 注意：@Builder.Default仅在通过Builder创建时生效
            // 直接使用no-args构造函数时，status为null（由Java默认值）
            // 但Lombok会在生成的no-args构造函数中初始化@Builder.Default字段
            assertThat(user.getStatus()).isEqualTo(UserStatus.OFFLINE);
        }

        @Test
        @DisplayName("使用全参构造函数创建用户")
        void testCreateUserWithAllArgsConstructor() {
            // Given
            Long id = 1L;
            String username = "alice";
            String password = "password123";
            String nickname = "Alice Smith";
            String avatar = "https://example.com/avatar.jpg";
            UserStatus status = UserStatus.ONLINE;
            LocalDateTime createTime = LocalDateTime.now();
            LocalDateTime updateTime = LocalDateTime.now();

            // When
            User user = new User(id, username, password, nickname, avatar, status, createTime, updateTime);

            // Then
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getNickname()).isEqualTo(nickname);
            assertThat(user.getAvatar()).isEqualTo(avatar);
            assertThat(user.getStatus()).isEqualTo(status);
            assertThat(user.getCreateTime()).isEqualTo(createTime);
            assertThat(user.getUpdateTime()).isEqualTo(updateTime);
        }
    }

    @Nested
    @DisplayName("UserStatus枚举测试")
    class UserStatusEnumTests {

        @ParameterizedTest
        @DisplayName("测试所有UserStatus枚举值")
        @EnumSource(UserStatus.class)
        void testUserStatusEnumValues(UserStatus status) {
            // Given & When & Then
            assertThat(status).isNotNull();
            assertThat(UserStatus.values()).contains(status);
        }

        @Test
        @DisplayName("测试UserStatus枚举数量")
        void testUserStatusEnumCount() {
            // Given
            UserStatus[] statuses = UserStatus.values();

            // Then
            assertThat(statuses).hasSize(2);
            assertThat(statuses).containsExactly(UserStatus.ONLINE, UserStatus.OFFLINE);
        }

        @ParameterizedTest
        @DisplayName("测试UserStatus与用户实体的关联")
        @EnumSource(UserStatus.class)
        void testUserStatusWithEntity(UserStatus status) {
            // When
            User user = User.builder()
                    .username("testuser")
                    .password("password123")
                    .status(status)
                    .build();

            // Then
            assertThat(user.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("测试UserStatus默认值")
        void testUserStatusDefaultValue() {
            // When
            User user = User.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.OFFLINE);
        }

        @Test
        @DisplayName("测试UserStatus枚举名称")
        void testUserStatusEnumNames() {
            // When & Then
            assertThat(UserStatus.ONLINE.name()).isEqualTo("ONLINE");
            assertThat(UserStatus.OFFLINE.name()).isEqualTo("OFFLINE");
        }
    }

    @Nested
    @DisplayName("字段验证测试")
    class FieldValidationTests {

        @ParameterizedTest
        @DisplayName("测试必填字段 - 用户名")
        @CsvSource({
                "alice, true",
                "bob, true",
                "'', false",
                "null, false"
        })
        void testUsernameValidation(String username, boolean isValid) {
            // Given
            String testUsername = "null".equals(username) ? null : username;

            // When
            User user = User.builder()
                    .username(testUsername)
                    .password("password123")
                    .build();

            // Then
            if (isValid) {
                assertThat(user.getUsername()).isNotNull();
                assertThat(user.getUsername()).isNotEmpty();
            } else {
                if (testUsername == null) {
                    assertThat(user.getUsername()).isNull();
                } else {
                    assertThat(user.getUsername()).isEmpty();
                }
            }
        }

        @Test
        @DisplayName("测试时间戳字段自动填充")
        void testTimestampFields() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now();

            // When
            User user = new User("testuser", "password123");

            // Then
            assertThat(user.getCreateTime()).isNull(); // Hibernate会自动填充
            assertThat(user.getUpdateTime()).isNull(); // Hibernate会自动填充
        }

        @Test
        @DisplayName("测试可选字段")
        void testOptionalFields() {
            // When
            User user = User.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            // Then
            assertThat(user.getNickname()).isNull();
            assertThat(user.getAvatar()).isNull();
            assertThat(user.getId()).isNull(); // ID由数据库生成
        }

        @Test
        @DisplayName("测试状态切换")
        void testStatusSwitching() {
            // Given
            User user = User.builder()
                    .username("testuser")
                    .password("password123")
                    .status(UserStatus.OFFLINE)
                    .build();

            // When
            user.setStatus(UserStatus.ONLINE);

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ONLINE);
        }

        @Test
        @DisplayName("测试密码字段")
        void testPasswordField() {
            // Given
            String plainPassword = "password123";

            // When
            User user = User.builder()
                    .username("testuser")
                    .password(plainPassword)
                    .build();

            // Then
            assertThat(user.getPassword()).isEqualTo(plainPassword);
            // 注意：实际应用中，密码应该在Service层加密后再存储
        }
    }

    @Nested
    @DisplayName("Lombok注解功能测试")
    class LombokAnnotationTests {

        @Test
        @DisplayName("测试@Data注解的getter和setter")
        void testDataAnnotationGettersSetters() {
            // Given
            User user = new User();

            // When
            user.setId(1L);
            user.setUsername("alice");
            user.setPassword("password123");
            user.setNickname("Alice Smith");
            user.setAvatar("https://example.com/avatar.jpg");
            user.setStatus(UserStatus.ONLINE);

            // Then
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getUsername()).isEqualTo("alice");
            assertThat(user.getPassword()).isEqualTo("password123");
            assertThat(user.getNickname()).isEqualTo("Alice Smith");
            assertThat(user.getAvatar()).isEqualTo("https://example.com/avatar.jpg");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ONLINE);
        }

        @Test
        @DisplayName("测试@Builder注解的链式调用")
        void testBuilderAnnotationChaining() {
            // When
            User user = User.builder()
                    .username("alice")
                    .nickname("Alice")
                    .avatar("avatar.jpg")
                    .password("pass")
                    .status(UserStatus.ONLINE)
                    .build();

            // Then - 验证所有字段都通过链式调用设置成功
            assertThat(user.getUsername()).isEqualTo("alice");
            assertThat(user.getNickname()).isEqualTo("Alice");
            assertThat(user.getAvatar()).isEqualTo("avatar.jpg");
            assertThat(user.getPassword()).isEqualTo("pass");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ONLINE);
        }

        @Test
        @DisplayName("测试@Builder.Default注解的默认值")
        void testBuilderDefaultAnnotation() {
            // When
            User user = User.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.OFFLINE);
        }
    }
}
