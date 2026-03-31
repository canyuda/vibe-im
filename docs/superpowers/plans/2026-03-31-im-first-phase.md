# IM即时通信产品 第一阶段实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建Java后端+Flutter前端的IM即时通信产品第一阶段，实现用户认证、点对点聊天和WebSocket实时通信功能

**Architecture:** 单体Spring Boot应用，采用模块化设计（认证模块、用户模块、聊天模块、WebSocket模块），使用Redis进行Session管理和在线状态管理，MySQL存储持久化数据

**Tech Stack:**
- 后端: Java 21, Spring Boot 3.2, Spring Data JPA, Spring WebSocket, MySQL 8.0, Redis 7.x
- 前端: Flutter 3.24, Provider状态管理, WebSocket客户端
- 测试: JUnit 5, Mockito, Spring Boot Test

---

## 文件结构映射

### 后端文件结构
```
backend/
├── pom.xml
├── src/main/java/com/vibe/im/
│   ├── VibeImApplication.java
│   ├── config/
│   │   ├── RedisConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebSocketConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── ChatController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   └── ChatService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── MessageRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   └── Message.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── SendMessageRequest.java
│   │   └── response/
│   │       ├── UserResponse.java
│   │       ├── LoginResponse.java
│   │       ├── MessageResponse.java
│   │       └── PageResponse.java
│   ├── websocket/
│   │   ├── WebSocketHandler.java
│   │   └── ConnectionManager.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   └── util/
│       └── SnowflakeIdGenerator.java
└── src/main/resources/
    └── application.yml
```

### 前端文件结构
```
frontend/
├── pubspec.yaml
├── lib/
│   ├── main.dart
│   ├── models/
│   │   ├── user.dart
│   │   └── message.dart
│   ├── services/
│   │   ├── api_service.dart
│   │   └── websocket_service.dart
│   ├── providers/
│   │   ├── auth_provider.dart
│   │   └── chat_provider.dart
│   └── pages/
│       ├── login_page.dart
│       ├── chat_list_page.dart
│       └── chat_page.dart
```

---

## 第一部分：项目初始化与环境搭建

### Task 1: 创建后端Maven项目结构

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/vibe/im/VibeImApplication.java`
- Create: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 创建pom.xml文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.vibe</groupId>
    <artifactId>vibe-im-backend</artifactId>
    <version>1.0.0</version>
    <name>Vibe IM Backend</name>
    <description>Instant Messaging Backend Service</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot WebSocket -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Data Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建主应用类**

```java
package com.vibe.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VibeImApplication {
    public static void main(String[] args) {
        SpringApplication.run(VibeImApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建application.yml配置文件**

```yaml
spring:
  application:
    name: vibe-im

  datasource:
    url: jdbc:mysql://localhost:3306/vibe_im?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    com.vibe.im: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

- [ ] **Step 4: 提交初始项目结构**

```bash
cd /Users/yiqi/IdeaProjects/vibeCodingDemo/backend
git add pom.xml src/
git commit -m "feat: initialize Spring Boot project structure"
```

---

### Task 2: 创建枚举和异常类

**Files:**
- Create: `backend/src/main/java/com/vibe/im/entity/enums/UserStatus.java`
- Create: `backend/src/main/java/com/vibe/im/entity/enums/MessageType.java`
- Create: `backend/src/main/java/com/vibe/im/entity/enums/MessageStatus.java`
- Create: `backend/src/main/java/com/vibe/im/exception/ErrorCode.java`
- Create: `backend/src/main/java/com/vibe/im/exception/BusinessException.java`
- Create: `backend/src/main/java/com/vibe/im/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建UserStatus枚举**

```java
package com.vibe.im.entity.enums;

public enum UserStatus {
    ONLINE,
    OFFLINE
}
```

- [ ] **Step 2: 创建MessageType枚举**

```java
package com.vibe.im.entity.enums;

public enum MessageType {
    TEXT,
    IMAGE,
    FILE
}
```

- [ ] **Step 3: 创建MessageStatus枚举**

```java
package com.vibe.im.entity.enums;

public enum MessageStatus {
    SENDING,
    SENT,
    DELIVERED
}
```

- [ ] **Step 4: 创建ErrorCode枚举**

```java
package com.vibe.im.exception;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    INVALID_PARAMETER(400, "参数错误"),
    AUTHENTICATION_FAILED(401, "认证失败"),
    USER_NOT_FOUND(404, "用户不存在"),
    USER_ALREADY_EXISTS(409, "用户已存在"),
    MESSAGE_NOT_FOUND(404, "消息不存在"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 5: 创建BusinessException类**

```java
package com.vibe.im.exception;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

- [ ] **Step 6: 创建GlobalExceptionHandler类**

```java
package com.vibe.im.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
            e.getErrorCode().getCode(),
            e.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(e.getErrorCode().getCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception: ", e);
        ErrorResponse response = new ErrorResponse(
            500,
            "服务器内部错误",
            LocalDateTime.now()
        );
        return ResponseEntity.status(500).body(response);
    }
}

record ErrorResponse(int code, String message, LocalDateTime timestamp) {}
```

- [ ] **Step 7: 提交枚举和异常类**

```bash
git add src/main/java/com/vibe/im/entity/enums/ src/main/java/com/vibe/im/exception/
git commit -m "feat: add enums and exception classes"
```

---

### Task 3: 创建Redis配置和Snowflake ID生成器

**Files:**
- Create: `backend/src/main/java/com/vibe/im/config/RedisConfig.java`
- Create: `backend/src/main/java/com/vibe/im/util/SnowflakeIdGenerator.java`

- [ ] **Step 1: 创建RedisConfig类**

```java
package com.vibe.im.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

- [ ] **Step 2: 创建SnowflakeIdGenerator类**

```java
package com.vibe.im.util;

import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {
    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this.workerId = 1L; // 简化配置，实际应从配置读取
    }

    public synchronized String generateId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return String.valueOf(((timestamp - EPOCH) << TIMESTAMP_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence);
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
```

- [ ] **Step 3: 提交Redis配置和ID生成器**

```bash
git add src/main/java/com/vibe/im/config/ src/main/java/com/vibe/im/util/
git commit -m "feat: add Redis config and Snowflake ID generator"
```

---

## 第二部分：用户认证与授权系统

### Task 4: 创建User实体和Repository

**Files:**
- Create: `backend/src/main/java/com/vibe/im/entity/User.java`
- Create: `backend/src/main/java/com/vibe/im/repository/UserRepository.java`
- Test: `backend/src/test/java/com/vibe/im/entity/UserTest.java`

- [ ] **Step 1: 创建User实体类**

```java
package com.vibe.im.entity;

import com.vibe.im.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "create_time", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 创建UserRepository接口**

```java
package com.vibe.im.repository;

import com.vibe.im.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

- [ ] **Step 3: 编写User实体测试**

```java
package com.vibe.im.entity;

import com.vibe.im.entity.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void userEntityCreation() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setNickname("Test User");
        user.setAvatar("http://example.com/avatar.jpg");
        user.setStatus(UserStatus.ONLINE);

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getNickname()).isEqualTo("Test User");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ONLINE);
        assertThat(user.getId()).isNull(); // ID尚未生成
    }

    @Test
    void userStatusEnumValues() {
        UserStatus[] statuses = UserStatus.values();
        assertThat(statuses).hasSize(2);
        assertThat(statuses).contains(UserStatus.ONLINE, UserStatus.OFFLINE);
    }
}
```

- [ ] **Step 4: 运行测试验证**

```bash
cd /Users/yiqi/IdeaProjects/vibeCodingDemo/backend
mvn test -Dtest=UserTest
```
Expected: PASS

- [ ] **Step 5: 提交User实体和Repository**

```bash
git add src/main/java/com/vibe/im/entity/ src/main/java/com/vibe/im/repository/ src/test/java/com/vibe/im/entity/
git commit -m "feat: add User entity and UserRepository"
```

---

### Task 5: 创建DTO类

**Files:**
- Create: `backend/src/main/java/com/vibe/im/dto/request/RegisterRequest.java`
- Create: `backend/src/main/java/com/vibe/im/dto/request/LoginRequest.java`
- Create: `backend/src/main/java/com/vibe/im/dto/response/UserResponse.java`
- Create: `backend/src/main/java/com/vibe/im/dto/response/LoginResponse.java`

- [ ] **Step 1: 创建RegisterRequest**

```java
package com.vibe.im.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度需3-20字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需6-20字符")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最多50字符")
    private String nickname;
}
```

- [ ] **Step 2: 创建LoginRequest**

```java
package com.vibe.im.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 3: 创建UserResponse**

```java
package com.vibe.im.dto.response;

import com.vibe.im.entity.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建LoginResponse**

```java
package com.vibe.im.dto.response;

import com.vibe.im.entity.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {

    private String sessionId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createTime;
}
```

- [ ] **Step 5: 提交DTO类**

```bash
git add src/main/java/com/vibe/im/dto/
git commit -m "feat: add request/response DTOs for authentication"
```

---

### Task 6: 实现AuthService

**Files:**
- Create: `backend/src/main/java/com/vibe/im/service/AuthService.java`
- Test: `backend/src/test/java/com/vibe/im/service/AuthServiceTest.java`

- [ ] **Step 1: 创建AuthService类**

```java
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SnowflakeIdGenerator idGenerator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_ONLINE_KEY_PREFIX = "user:online:";
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(UserStatus.OFFLINE);

        user = userRepository.save(user);

        log.info("User registered: {}", user.getUsername());
        return toUserResponse(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String sessionId = UUID.randomUUID().toString();

        // 保存Session
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForHash().put(sessionKey, "userId", user.getId());
        redisTemplate.opsForHash().put(sessionKey, "username", user.getUsername());
        redisTemplate.expire(sessionKey, SESSION_TTL);

        // 更新用户在线状态
        String onlineKey = USER_ONLINE_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(onlineKey, UserStatus.ONLINE, Duration.ofMinutes(30));

        // 更新数据库用户状态
        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);

        log.info("User logged in: {}", user.getUsername());

        return toLoginResponse(user, sessionId);
    }

    public void logout(String sessionId, Long userId) {
        if (sessionId == null) {
            return;
        }

        // 删除Session
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(sessionKey);

        // 更新用户离线状态
        if (userId != null) {
            String onlineKey = USER_ONLINE_KEY_PREFIX + userId;
            redisTemplate.delete(onlineKey);

            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.OFFLINE);
                userRepository.save(user);
            });
        }

        log.info("User logged out: userId={}", userId);
    }

    public Long getUserIdBySession(String sessionId) {
        if (sessionId == null) {
            return null;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        Object userIdObj = redisTemplate.opsForHash().get(sessionKey, "userId");

        if (userIdObj != null) {
            // 续期Session
            redisTemplate.expire(sessionKey, SESSION_TTL);
            return ((Number) userIdObj).longValue();
        }

        return null;
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        return response;
    }

    private LoginResponse toLoginResponse(User user, String sessionId) {
        LoginResponse response = new LoginResponse();
        response.setSessionId(sessionId);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        return response;
    }
}
```

- [ ] **Step 2: 编写AuthService测试**

```java
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SnowflakeIdGenerator idGenerator;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMy");
        testUser.setNickname("Test User");
        testUser.setStatus(UserStatus.OFFLINE);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void registerNewUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setNickname("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerExistingUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("Test User");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.login(request);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getSessionId()).isNotEmpty();
        assertThat(response.getStatus()).isEqualTo(UserStatus.ONLINE);
        verify(hashOperations).put(anyString(), eq("userId"), eq(1L));
        verify(hashOperations).expire(anyString(), any(Duration.class));
        verify(userRepository).save(testUser);
    }

    @Test
    void loginWithWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void loginWithNonExistentUser() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void logoutSuccess() {
        authService.logout("session123", 1L);

        verify(redisTemplate).delete("session:session123");
        verify(redisTemplate).delete("user:online:1");
        verify(hashOperations).put(anyString(), eq("userId"), eq(1L));
    }
}
```

- [ ] **Step 3: 运行测试验证**

```bash
mvn test -Dtest=AuthServiceTest
```
Expected: All tests PASS

- [ ] **Step 4: 提交AuthService**

```bash
git add src/main/java/com/vibe/im/service/ src/test/java/com/vibe/im/service/
git commit -m "feat: implement AuthService with registration and login"
```

---

### Task 7: 实现AuthController

**Files:**
- Create: `backend/src/main/java/com/vibe/im/controller/AuthController.java`
- Test: `backend/src/test/java/com/vibe/im/controller/AuthControllerTest.java`

- [ ] **Step 1: 创建AuthController类**

```java
package com.vibe.im.controller;

import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.UserResponse;
import com.vibe.im.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request: username={}", request.getUsername());
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request: username={}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader(value = "Session-Id", required = false) String sessionId,
        @RequestHeader(value = "User-Id", required = false) Long userId
    ) {
        log.info("Logout request: sessionId={}, userId={}", sessionId, userId);
        authService.logout(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
        @RequestHeader(value = "Session-Id", required = false) String sessionId
    ) {
        Long userId = authService.getUserIdBySession(sessionId);
        if (userId == null) {
            throw new com.vibe.im.exception.BusinessException(
                com.vibe.im.exception.ErrorCode.AUTHENTICATION_FAILED
            );
        }

        // 这里需要UserService来获取用户信息，暂时简化
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 2: 编写AuthController测试**

```java
package com.vibe.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void registerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("Test User");

        when(authService.register(any(RegisterRequest.class)))
            .thenReturn(new com.vibe.im.dto.response.UserResponse());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void registerWithInvalidUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // 太短
        request.setPassword("password123");
        request.setNickname("Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        LoginResponse response = new LoginResponse();
        response.setSessionId("session123");
        response.setUsername("testuser");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value("session123"))
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void logoutSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Session-Id", "session123")
                .header("User-Id", "1"))
            .andExpect(status().isOk());
    }
}
```

- [ ] **Step 3: 运行测试验证**

```bash
mvn test -Dtest=AuthControllerTest
```
Expected: All tests PASS

- [ ] **Step 4: 提交AuthController**

```bash
git add src/main/java/com/vibe/im/controller/ src/test/java/com/vibe/im/controller/
git commit -m "feat: implement AuthController with registration and login endpoints"
```

---

## 第三部分：基础聊天功能实现

### Task 8: 创建Message实体和Repository

**Files:**
- Create: `backend/src/main/java/com/vibe/im/entity/Message.java`
- Create: `backend/src/main/java/com/vibe/im/repository/MessageRepository.java`

- [ ] **Step 1: 创建Message实体类**

```java
package com.vibe.im.entity;

import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.entity.enums.MessageType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true, length = 50)
    private String messageId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENDING;

    @Column(name = "create_time", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 创建MessageRepository接口**

```java
package com.vibe.im.repository;

import com.vibe.im.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
           "ORDER BY m.createTime DESC")
    Page<Message> findChatMessages(Long userId1, Long userId2, Pageable pageable);

    List<Message> findByReceiverIdAndStatus(Long receiverId, MessageStatus status);
}
```

- [ ] **Step 3: 提交Message实体和Repository**

```bash
git add src/main/java/com/vibe/im/entity/Message.java src/main/java/com/vibe/im/repository/MessageRepository.java
git commit -m "feat: add Message entity and MessageRepository"
```

---

### Task 9: 创建聊天相关DTO

**Files:**
- Create: `backend/src/main/java/com/vibe/im/dto/request/SendMessageRequest.java`
- Create: `backend/src/main/java/com/vibe/im/dto/response/MessageResponse.java`
- Create: `backend/src/main/java/com/vibe/im/dto/response/PageResponse.java`

- [ ] **Step 1: 创建SendMessageRequest**

```java
package com.vibe.im.dto.request;

import com.vibe.im.entity.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息最多1000字符")
    private String content;

    private MessageType messageType = MessageType.TEXT;
}
```

- [ ] **Step 2: 创建MessageResponse**

```java
package com.vibe.im.dto.response;

import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {

    private Long id;
    private String messageId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 创建PageResponse**

```java
package com.vibe.im.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;

    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize()
        );
    }
}
```

- [ ] **Step 4: 提交聊天DTO**

```bash
git add src/main/java/com/vibe/im/dto/
git commit -m "feat: add chat request/response DTOs"
```

---

### Task 10: 实现ChatService

**Files:**
- Create: `backend/src/main/java/com/vibe/im/service/ChatService.java`

- [ ] **Step 1: 创建ChatService类**

```java
package com.vibe.im.service;

import com.vibe.im.dto.request.SendMessageRequest;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.dto.response.PageResponse;
import com.vibe.im.entity.Message;
import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.repository.MessageRepository;
import com.vibe.im.repository.UserRepository;
import com.vibe.im.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        // 验证接收者存在
        if (!userRepository.existsById(request.getReceiverId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证不能给自己发消息
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        Message message = new Message();
        message.setMessageId(idGenerator.generateId());
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setStatus(MessageStatus.SENDING);

        message = messageRepository.save(message);
        log.info("Message saved: messageId={}", message.getMessageId());

        // 这里触发WebSocket推送，稍后实现
        message.setStatus(MessageStatus.SENT);
        messageRepository.save(message);

        return toMessageResponse(message);
    }

    public PageResponse<MessageResponse> getChatMessages(Long userId, Long friendId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Message> messages = messageRepository.findChatMessages(userId, friendId, pageable);

        // 获取发送者ID列表用于批量查询用户名
        List<Long> senderIds = messages.getContent().stream()
            .map(Message::getSenderId)
            .distinct()
            .toList();

        // 简化：这里暂时只返回messageId，实际需要查询用户名
        List<MessageResponse> responses = messages.getContent().stream()
            .map(this::toMessageResponse)
            .toList();

        // 按时间正序返回
        responses = responses.stream()
            .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
            .collect(Collectors.toList());

        return new PageResponse<>(
            responses,
            messages.getNumber(),
            messages.getTotalPages(),
            messages.getTotalElements(),
            messages.getSize()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setMessageId(message.getMessageId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setStatus(message.getStatus());
        response.setCreateTime(message.getCreateTime());
        return response;
    }
}
```

- [ ] **Step 2: 提交ChatService**

```bash
git add src/main/java/com/vibe/im/service/ChatService.java
git commit -m "feat: implement ChatService for message sending and history"
```

---

### Task 11: 实现ChatController

**Files:**
- Create: `backend/src/main/java/com/vibe/im/controller/ChatController.java`

- [ ] **Step 1: 创建ChatController类**

```java
package com.vibe.im.controller;

import com.vibe.im.dto.request.SendMessageRequest;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.dto.response.PageResponse;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.service.AuthService;
import com.vibe.im.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
        @RequestHeader(value = "Session-Id", required = false) String sessionId,
        @Valid @RequestBody SendMessageRequest request
    ) {
        Long userId = authService.getUserIdBySession(sessionId);
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        log.info("Send message: senderId={}, receiverId={}", userId, request.getReceiverId());
        MessageResponse response = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getChatMessages(
        @RequestHeader(value = "Session-Id", required = false) String sessionId,
        @RequestParam("friendId") Long friendId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Long userId = authService.getUserIdBySession(sessionId);
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        PageResponse<MessageResponse> response = chatService.getChatMessages(userId, friendId, page, size);
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 2: 提交ChatController**

```bash
git add src/main/java/com/vibe/im/controller/ChatController.java
git commit -m "feat: implement ChatController with send and history endpoints"
```

---

### Task 12: 实现ConnectionManager

**Files:**
- Create: `backend/src/main/java/com/vibe/im/websocket/ConnectionManager.java`

- [ ] **Step 1: 创建ConnectionManager类**

```java
package com.vibe.im.websocket;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ConnectionManager {

    private static final Map<String, UserConnection> connections = new ConcurrentHashMap<>();
    private static final Map<Long, String> userIdToSessionId = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    static {
        // 每30秒检查一次心跳超时
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            connections.values().removeIf(conn -> {
                if (now - conn.getLastHeartbeat() > 30000) {
                    log.warn("Heartbeat timeout for userId={}, sessionId={}", conn.getUserId(), conn.getSessionId());
                    try {
                        conn.getSession().close();
                    } catch (IOException e) {
                        log.error("Error closing timeout session", e);
                    }
                    userIdToSessionId.remove(conn.getUserId());
                    return true;
                }
                return false;
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    public void addConnection(Long userId, String sessionId, WebSocketSession session) {
        UserConnection connection = new UserConnection(userId, sessionId, session);
        connections.put(sessionId, connection);
        userIdToSessionId.put(userId, sessionId);
        log.info("Connection added: userId={}, sessionId={}", userId, sessionId);
    }

    public void removeConnection(String sessionId) {
        UserConnection connection = connections.remove(sessionId);
        if (connection != null) {
            userIdToSessionId.remove(connection.getUserId());
            log.info("Connection removed: userId={}, sessionId={}", connection.getUserId(), sessionId);
        }
    }

    public UserConnection getConnection(String sessionId) {
        return connections.get(sessionId);
    }

    public UserConnection getConnectionByUserId(Long userId) {
        String sessionId = userIdToSessionId.get(userId);
        return sessionId != null ? connections.get(sessionId) : null;
    }

    public boolean isUserOnline(Long userId) {
        return userIdToSessionId.containsKey(userId);
    }

    public void sendMessageToUser(Long userId, String message) throws IOException {
        UserConnection connection = getConnectionByUserId(userId);
        if (connection != null) {
            connection.getSession().sendMessage(new TextMessage(message));
            log.debug("Message sent to userId={}", userId);
        }
    }

    public void updateHeartbeat(String sessionId) {
        UserConnection connection = connections.get(sessionId);
        if (connection != null) {
            connection.setLastHeartbeat(System.currentTimeMillis());
        }
    }

    @Data
    public static class UserConnection {
        private final Long userId;
        private final String sessionId;
        private final WebSocketSession session;
        private long lastHeartbeat;

        public UserConnection(Long userId, String sessionId, WebSocketSession session) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.session = session;
            this.lastHeartbeat = System.currentTimeMillis();
        }
    }
}
```

- [ ] **Step 2: 提交ConnectionManager**

```bash
git add src/main/java/com/vibe/im/websocket/ConnectionManager.java
git commit -m "feat: implement ConnectionManager for WebSocket session management"
```

---

### Task 13: 实现WebSocketHandler和配置

**Files:**
- Create: `backend/src/main/java/com/vibe/im/config/WebSocketConfig.java`
- Create: `backend/src/main/java/com/vibe/im/websocket/WebSocketHandler.java`

- [ ] **Step 1: 创建WebSocketConfig类**

```java
package com.vibe.im.config;

import com.vibe.im.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
            .setAllowedOrigins("*");
    }
}
```

- [ ] **Step 2: 创建WebSocketHandler类**

```java
package com.vibe.im.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.entity.Message;
import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.repository.MessageRepository;
import com.vibe.im.service.AuthService;
import com.vibe.im.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ConnectionManager connectionManager;
    private final AuthService authService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getUri().getQuery();
        Long userId = extractUserId(sessionId);

        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid session"));
            return;
        }

        connectionManager.addConnection(userId, sessionId, session);
        log.info("WebSocket connected: userId={}, sessionId={}", userId, sessionId);

        // 发送离线消息
        sendOfflineMessages(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonNode jsonNode = objectMapper.readTree(payload);

        String type = jsonNode.has("type") ? jsonNode.get("type").asText() : null;

        if ("PING".equals(type)) {
            connectionManager.updateHeartbeat(session.getId());
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectionManager.removeConnection(session.getId());
        log.info("WebSocket disconnected: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: sessionId={}", session.getId(), exception);
        connectionManager.removeConnection(session.getId());
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query == null || !query.contains("sessionId=")) {
            return null;
        }

        String sessionId = query.split("sessionId=")[1].split("&")[0];
        return authService.getUserIdBySession(sessionId);
    }

    private void sendOfflineMessages(Long userId, WebSocketSession session) throws Exception {
        List<Message> offlineMessages = messageRepository
            .findByReceiverIdAndStatus(userId, MessageStatus.SENT);

        for (Message message : offlineMessages) {
            message.setStatus(MessageStatus.DELIVERED);
            MessageResponse response = toMessageResponse(message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

        messageRepository.saveAll(offlineMessages);
        log.info("Sent {} offline messages to userId={}", offlineMessages.size(), userId);
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setMessageId(message.getMessageId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setStatus(message.getStatus());
        response.setCreateTime(message.getCreateTime());
        return response;
    }
}
```

- [ ] **Step 3: 提交WebSocket配置和Handler**

```bash
git add src/main/java/com/vibe/im/config/WebSocketConfig.java src/main/java/com/vibe/im/websocket/WebSocketHandler.java
git commit -m "feat: implement WebSocket configuration and message handler"
```

---

### Task 14: 集成WebSocket消息推送

**Files:**
- Modify: `backend/src/main/java/com/vibe/im/service/ChatService.java`

- [ ] **Step 1: 修改ChatService集成WebSocket推送**

```java
package com.vibe.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.request.SendMessageRequest;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.dto.response.PageResponse;
import com.vibe.im.entity.Message;
import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.repository.MessageRepository;
import com.vibe.im.repository.UserRepository;
import com.vibe.im.util.SnowflakeIdGenerator;
import com.vibe.im.websocket.ConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final ConnectionManager connectionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        if (!userRepository.existsById(request.getReceiverId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        Message message = new Message();
        message.setMessageId(idGenerator.generateId());
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setStatus(MessageStatus.SENDING);

        message = messageRepository.save(message);
        log.info("Message saved: messageId={}", message.getMessageId());

        MessageResponse response = toMessageResponse(message);

        // 推送消息给接收者
        try {
            if (connectionManager.isUserOnline(request.getReceiverId())) {
                String messageJson = objectMapper.writeValueAsString(response);
                connectionManager.sendMessageToUser(request.getReceiverId(), messageJson);
                message.setStatus(MessageStatus.DELIVERED);
            } else {
                message.setStatus(MessageStatus.SENT);
            }
            messageRepository.save(message);
        } catch (IOException e) {
            log.error("Failed to send message via WebSocket", e);
            message.setStatus(MessageStatus.SENT);
            messageRepository.save(message);
        }

        return response;
    }

    public PageResponse<MessageResponse> getChatMessages(Long userId, Long friendId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Message> messages = messageRepository.findChatMessages(userId, friendId, pageable);

        List<MessageResponse> responses = messages.getContent().stream()
            .map(this::toMessageResponse)
            .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
            .collect(Collectors.toList());

        return new PageResponse<>(
            responses,
            messages.getNumber(),
            messages.getTotalPages(),
            messages.getTotalElements(),
            messages.getSize()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setMessageId(message.getMessageId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setStatus(message.getStatus());
        response.setCreateTime(message.getCreateTime());
        return response;
    }
}
```

- [ ] **Step 2: 提交WebSocket集成**

```bash
git add src/main/java/com/vibe/im/service/ChatService.java
git commit -m "feat: integrate WebSocket message pushing in ChatService"
```

---

## 第四部分：Flutter前端实现

### Task 15: 创建Flutter项目基础结构

**Files:**
- Create: `frontend/pubspec.yaml`
- Create: `frontend/lib/main.dart`
- Create: `frontend/lib/models/user.dart`
- Create: `frontend/lib/models/message.dart`

- [ ] **Step 1: 创建pubspec.yaml**

```yaml
name: vibe_im
description: Vibe IM Flutter Application
publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: '>=3.0.0 <4.0.0'

dependencies:
  flutter:
    sdk: flutter
  provider: ^6.1.1
  http: ^1.1.0
  web_socket_channel: ^2.4.0
  shared_preferences: ^2.2.2
  intl: ^0.18.1

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.0

flutter:
  uses-material-design: true
```

- [ ] **Step 2: 创建main.dart**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/auth_provider.dart';
import 'providers/chat_provider.dart';
import 'pages/login_page.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
      ],
      child: MaterialApp(
        title: 'Vibe IM',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
          useMaterial3: true,
        ),
        home: const LoginPage(),
      ),
    );
  }
}
```

- [ ] **Step 3: 创建user.dart模型**

```dart
class User {
  final int id;
  final String username;
  final String nickname;
  final String? avatar;
  final String status;
  final DateTime createTime;

  User({
    required this.id,
    required this.username,
    required this.nickname,
    this.avatar,
    required this.status,
    required this.createTime,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int,
      username: json['username'] as String,
      nickname: json['nickname'] as String,
      avatar: json['avatar'] as String?,
      status: json['status'] as String,
      createTime: DateTime.parse(json['createTime'] as String),
    );
  }
}
```

- [ ] **Step 4: 创建message.dart模型**

```dart
class Message {
  final int id;
  final String messageId;
  final int senderId;
  final String senderName;
  final int receiverId;
  final String content;
  final String messageType;
  final String status;
  final DateTime createTime;

  Message({
    required this.id,
    required this.messageId,
    required this.senderId,
    required this.senderName,
    required this.receiverId,
    required this.content,
    required this.messageType,
    required this.status,
    required this.createTime,
  });

  factory Message.fromJson(Map<String, dynamic> json) {
    return Message(
      id: json['id'] as int,
      messageId: json['messageId'] as String,
      senderId: json['senderId'] as int,
      senderName: json['senderName'] as String? ?? '',
      receiverId: json['receiverId'] as int,
      content: json['content'] as String,
      messageType: json['messageType'] as String,
      status: json['status'] as String,
      createTime: DateTime.parse(json['createTime'] as String),
    );

    get isSent => status == 'SENT' || status == 'DELIVERED';
  }
}
```

- [ ] **Step 5: 提交Flutter基础结构**

```bash
cd /Users/yiqi/IdeaProjects/vibeCodingDemo/frontend
git add pubspec.yaml lib/main.dart lib/models/
git commit -m "feat: initialize Flutter project structure"
```

---

### Task 16: 创建API服务

**Files:**
- Create: `frontend/lib/services/api_service.dart`

- [ ] **Step 1: 创建api_service.dart**

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user.dart';
import '../models/message.dart';

class ApiService {
  static const String baseUrl = 'http://localhost:8080/api';

  String? _sessionId;
  int? _currentUserId;

  String get sessionId => _sessionId ?? '';
  int? get currentUserId => _currentUserId;

  void setSession(String sessionId, int userId) {
    _sessionId = sessionId;
    _currentUserId = userId;
  }

  void clearSession() {
    _sessionId = null;
    _currentUserId = null;
  }

  Map<String, String> get _headers => {
    'Content-Type': 'application/json',
    if (_sessionId != null) 'Session-Id': _sessionId!,
    if (_currentUserId != null) 'User-Id': _currentUserId.toString(),
  };

  Future<User> register(String username, String password, String nickname) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'password': password,
        'nickname': nickname,
      }),
    );

    if (response.statusCode == 200) {
      return User.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Registration failed');
    }
  }

  Future<Map<String, dynamic>> login(String username, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'password': password,
      }),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      setSession(data['sessionId'], data['userId']);
      return data;
    } else {
      throw Exception('Login failed');
    }
  }

  Future<void> logout() async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/logout'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      clearSession();
    }
  }

  Future<Message> sendMessage(int receiverId, String content) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/send'),
      headers: _headers,
      body: jsonEncode({
        'receiverId': receiverId,
        'content': content,
        'messageType': 'TEXT',
      }),
    );

    if (response.statusCode == 200) {
      return Message.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to send message');
    }
  }

  Future<List<Message>> getChatMessages(int friendId, {int page = 0, int size = 20}) async {
    final response = await http.get(
      Uri.parse('$baseUrl/chat/messages?friendId=$friendId&page=$page&size=$size'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      final List<dynamic> content = data['content'];
      return content.map((msg) => Message.fromJson(msg)).toList();
    } else {
      throw Exception('Failed to load messages');
    }
  }
}
```

- [ ] **Step 2: 提交API服务**

```bash
git add lib/services/api_service.dart
git commit -m "feat: implement API service for backend communication"
```

---

### Task 17: 创建WebSocket服务

**Files:**
- Create: `frontend/lib/services/websocket_service.dart`

- [ ] **Step 1: 创建websocket_service.dart**

```dart
import 'dart:async';
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../models/message.dart';
import 'api_service.dart';

class WebSocketService {
  static WebSocketService? _instance;
  WebSocketChannel? _channel;
  StreamSubscription? _subscription;

  final ApiService _apiService = ApiService();
  Timer? _heartbeatTimer;

  final _messageController = StreamController<Message>.broadcast();

  Stream<Message> get messageStream => _messageController.stream;

  static WebSocketService get instance {
    _instance ??= WebSocketService();
    return _instance!;
  }

  Future<void> connect() async {
    if (_channel != null && _apiService.sessionId.isNotEmpty) {
      return;
    }

    final wsUrl = Uri.parse(
      'ws://localhost:8080/api/ws?sessionId=${_apiService.sessionId}',
    );

    _channel = WebSocketChannel.connect(wsUrl);

    _subscription = _channel!.stream.listen(
      _handleMessage,
      onError: _handleError,
      onDone: _handleDisconnect,
    );

    _startHeartbeat();
  }

  void _handleMessage(dynamic message) {
    try {
      final data = jsonDecode(message);
      final type = data['type'];

      if (type == 'PONG') {
        return;
      }

      if (data['messageId'] != null) {
        final msg = Message.fromJson(data);
        _messageController.add(msg);
      }
    } catch (e) {
      print('Error parsing WebSocket message: $e');
    }
  }

  void _handleError(error) {
    print('WebSocket error: $error');
  }

  void _handleDisconnect() {
    print('WebSocket disconnected');
    _stopHeartbeat();
    _channel = null;
    _subscription?.cancel();
  }

  void _startHeartbeat() {
    _heartbeatTimer = Timer.periodic(const Duration(seconds: 15), (_) {
      _sendPing();
    });
  }

  void _stopHeartbeat() {
    _heartbeatTimer?.cancel();
    _heartbeatTimer = null;
  }

  void _sendPing() {
    if (_channel != null) {
      _channel!.sink.add(jsonEncode({'type': 'PING'}));
    }
  }

  void disconnect() {
    _channel?.sink.close();
    _handleDisconnect();
  }

  void dispose() {
    _messageController.close();
    disconnect();
  }
}
```

- [ ] **Step 2: 提交WebSocket服务**

```bash
git add lib/services/websocket_service.dart
git commit -m "feat: implement WebSocket service for real-time messaging"
```

---

### Task 18: 创建AuthProvider

**Files:**
- Create: `frontend/lib/providers/auth_provider.dart`

- [ ] **Step 1: 创建auth_provider.dart**

```dart
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/api_service.dart';
import '../models/user.dart';

class AuthProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  User? _currentUser;

  User? get currentUser => _currentUser;
  bool get isLoggedIn => _currentUser != null;

  Future<bool> register(String username, String password, String nickname) async {
    try {
      _currentUser = await _apiService.register(username, password, nickname);
      notifyListeners();
      return true;
    } catch (e) {
      debugPrint('Registration error: $e');
      return false;
    }
  }

  Future<bool> login(String username, String password) async {
    try {
      final data = await _apiService.login(username, password);
      _currentUser = User(
        id: data['userId'] as int,
        username: data['username'] as String,
        nickname: data['nickname'] as String,
        avatar: data['avatar'] as String?,
        status: data['status'] as String,
        createTime: DateTime.parse(data['createTime'] as String),
      );

      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('sessionId', data['sessionId']);
      await prefs.setInt('userId', data['userId']);

      notifyListeners();
      return true;
    } catch (e) {
      debugPrint('Login error: $e');
      return false;
    }
  }

  Future<void> logout() async {
    try {
      await _apiService.logout();
    } catch (e) {
      debugPrint('Logout error: $e');
    }

    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('sessionId');
    await prefs.remove('userId');

    _currentUser = null;
    notifyListeners();
  }

  Future<void> loadSavedSession() async {
    final prefs = await SharedPreferences.getInstance();
    final sessionId = prefs.getString('sessionId');
    final userId = prefs.getInt('userId');

    if (sessionId != null && userId != null) {
      _apiService.setSession(sessionId, userId);
      // TODO: 验证session是否仍然有效
    }
  }
}
```

- [ ] **Step 2: 提交AuthProvider**

```bash
git add lib/providers/auth_provider.dart
git commit -m "feat: implement AuthProvider for state management"
```

---

### Task 19: 创建ChatProvider

**Files:**
- Create: `frontend/lib/providers/chat_provider.dart`

- [ ] **Step 1: 创建chat_provider.dart**

```dart
import 'dart:async';
import 'package:flutter/foundation.dart';
import '../services/api_service.dart';
import '../services/websocket_service.dart';
import '../models/message.dart';

class ChatProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  final WebSocketService _wsService = WebSocketService.instance;
  StreamSubscription? _wsSubscription;

  final List<Message> _messages = [];
  Map<int, List<Message>> _chatHistory = {};

  List<Message> get messages => _messages;
  List<Message> getMessagesForUser(int userId) =>
      _chatHistory[userId] ?? [];

  ChatProvider() {
    _initializeWebSocket();
  }

  void _initializeWebSocket() {
    _wsSubscription = _wsService.messageStream.listen((message) {
      _addMessage(message);
    });
  }

  Future<void> connectWebSocket() async {
    await _wsService.connect();
  }

  Future<void> sendMessage(int receiverId, String content) async {
    try {
      final message = await _apiService.sendMessage(receiverId, content);
      _addMessage(message);
    } catch (e) {
      debugPrint('Send message error: $e');
    }
  }

  Future<void> loadChatHistory(int friendId) async {
    try {
      final messages = await _apiService.getChatMessages(friendId);
      _chatHistory[friendId] = messages.reversed.toList();
      notifyListeners();
    } catch (e) {
      debugPrint('Load chat history error: $e');
    }
  }

  void _addMessage(Message message) {
    final userId = message.senderId == _apiService.currentUserId
        ? message.receiverId
        : message.senderId;

    if (!_chatHistory.containsKey(userId)) {
      _chatHistory[userId] = [];
    }

    // 避免重复消息
    final exists = _chatHistory[userId]!.any((m) => m.messageId == message.messageId);
    if (!exists) {
      _chatHistory[userId]!.add(message);
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _wsSubscription?.cancel();
    super.dispose();
  }
}
```

- [ ] **Step 2: 提交ChatProvider**

```bash
git add lib/providers/chat_provider.dart
git commit -m "feat: implement ChatProvider for chat state management"
```

---

### Task 20: 创建登录页面

**Files:**
- Create: `frontend/lib/pages/login_page.dart`

- [ ] **Step 1: 创建login_page.dart**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _nicknameController = TextEditingController();
  bool _isLoginMode = true;
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    _nicknameController.dispose();
    super.dispose();
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    final authProvider = context.read<AuthProvider>();

    try {
      bool success;
      if (_isLoginMode) {
        success = await authProvider.login(
          _usernameController.text.trim(),
          _passwordController.text,
        );
      } else {
        success = await authProvider.register(
          _usernameController.text.trim(),
          _passwordController.text,
          _nicknameController.text.trim(),
        );
      }

      if (success && mounted) {
        // TODO: 导航到聊天列表页面
      }
    } catch (e) {
      setState(() {
        _errorMessage = _isLoginMode ? '登录失败' : '注册失败';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  void _toggleMode() {
    setState(() {
      _isLoginMode = !_isLoginMode;
      _errorMessage = null;
      _formKey.currentState?.reset();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Icon(
                    Icons.chat_bubble_outline,
                    size: 80,
                    color: Colors.blue,
                  ),
                  const SizedBox(height: 48),
                  Text(
                    _isLoginMode ? '欢迎回来' : '创建账号',
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                  const SizedBox(height: 48),
                  TextFormField(
                    controller: _usernameController,
                    decoration: const InputDecoration(
                      labelText: '用户名',
                      prefixIcon: Icon(Icons.person),
                      border: OutlineInputBorder(),
                    ),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return '请输入用户名';
                      }
                      if (value.length < 3) {
                        return '用户名至少3个字符';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _passwordController,
                    obscureText: true,
                    decoration: const InputDecoration(
                      labelText: '密码',
                      prefixIcon: Icon(Icons.lock),
                      border: OutlineInputBorder(),
                    ),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return '请输入密码';
                      }
                      if (value.length < 6) {
                        return '密码至少6个字符';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  if (!_isLoginMode)
                    TextFormField(
                      controller: _nicknameController,
                      decoration: const InputDecoration(
                        labelText: '昵称',
                        prefixIcon: Icon(Icons.badge),
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return '请输入昵称';
                        }
                        return null;
                      },
                    ),
                  const SizedBox(height: 24),
                  if (_errorMessage != null)
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.red.shade50,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.error, color: Colors.red, size: 20),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              _errorMessage!,
                              style: const TextStyle(color: Colors.red),
                            ),
                          ),
                        ],
                      ),
                    ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _isLoading ? null : _handleSubmit,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                    child: _isLoading
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : Text(_isLoginMode ? '登录' : '注册'),
                  ),
                  const SizedBox(height: 16),
                  TextButton(
                    onPressed: _toggleMode,
                    child: Text(_isLoginMode
                        ? '没有账号？立即注册'
                        : '已有账号？立即登录'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
```

- [ ] **Step 2: 提交登录页面**

```bash
git add lib/pages/login_page.dart
git commit -m "feat: implement login page with registration"
```

---

### Task 21: 创建聊天列表页面

**Files:**
- Create: `frontend/lib/pages/chat_list_page.dart`

- [ ] **Step 1: 创建chat_list_page.dart**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import 'chat_page.dart';

class ChatListPage extends StatefulWidget {
  const ChatListPage({super.key});

  @override
  State<ChatListPage> createState() => _ChatListPageState();
}

class _ChatListPageState extends State<ChatListPage> {
  // TODO: 替换为实际的用户列表数据
  final List<Map<String, dynamic>> _contacts = [
    {'id': 2, 'username': 'user2', 'nickname': '李四', 'status': 'ONLINE'},
    {'id': 3, 'username': 'user3', 'nickname': '王五', 'status': 'OFFLINE'},
  ];

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('消息'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => _showLogoutDialog(context),
            tooltip: '退出登录',
          ),
        ],
      ),
      body: _contacts.isEmpty
          ? const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.chat_bubble_outline, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text(
                    '暂无消息',
                    style: TextStyle(fontSize: 16, color: Colors.grey),
                  ),
                ],
              ),
            )
          : ListView.builder(
              itemCount: _contacts.length,
              itemBuilder: (context, index) {
                final contact = _contacts[index];
                return _buildContactTile(context, contact);
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddContactDialog,
        child: const Icon(Icons.add),
        tooltip: '添加好友',
      ),
    );
  }

  Widget _buildContactTile(BuildContext context, Map<String, dynamic> contact) {
    final isOnline = contact['status'] == 'ONLINE';

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: isOnline ? Colors.green : Colors.grey,
        child: Text(
          contact['nickname'][0],
          style: const TextStyle(color: Colors.white, fontSize: 20),
        ),
      ),
      title: Text(contact['nickname']),
      subtitle: isOnline ? const Text('在线') : const Text('离线'),
      trailing: Icon(
        Icons.chevron_right,
        color: Colors.grey.shade400,
      ),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => ChatPage(
              userId: contact['id'],
              userName: contact['username'],
              userNickname: contact['nickname'],
            ),
          ),
        );
      },
    );
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              context.read<AuthProvider>().logout();
              Navigator.pop(context);
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  void _showAddContactDialog() {
    // TODO: 实现添加好友功能
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('添加好友功能待实现')),
    );
  }
}
```

- [ ] **Step 2: 提交聊天列表页面**

```bash
git add lib/pages/chat_list_page.dart
git commit -m "feat: implement chat list page"
```

---

### Task 22: 创建聊天页面

**Files:**
- Create: `frontend/lib/pages/chat_page.dart`

- [ ] **Step 1: 创建chat_page.dart**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/chat_provider.dart';
import '../models/message.dart';

class ChatPage extends StatefulWidget {
  final int userId;
  final String userName;
  final String userNickname;

  const ChatPage({
    super.key,
    required this.userId,
    required this.userName,
    required this.userNickname,
  });

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadChatHistory();
    _connectWebSocket();
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _loadChatHistory() async {
    setState(() => _isLoading = true);

    final chatProvider = context.read<ChatProvider>();
    await chatProvider.loadChatHistory(widget.userId);

    setState(() => _isLoading = false);
    _scrollToBottom();
  }

  Future<void> _connectWebSocket() async {
    final chatProvider = context.read<ChatProvider>();
    await chatProvider.connectWebSocket();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  Future<void> _sendMessage() async {
    final text = _messageController.text.trim();
    if (text.isEmpty) return;

    _messageController.clear();

    final chatProvider = context.read<ChatProvider>();
    await chatProvider.sendMessage(widget.userId, text);

    _scrollToBottom();
  }

  @override
  Widget build(BuildContext context) {
    final chatProvider = context.watch<ChatProvider>();
    final messages = chatProvider.getMessagesForUser(widget.userId);

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.userNickname),
      ),
      body: Column(
        children: [
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : messages.isEmpty
                    ? const Center(
                        child: Text(
                          '开始聊天吧',
                          style: TextStyle(color: Colors.grey),
                        ),
                      )
                    : ListView.builder(
                        controller: _scrollController,
                        padding: const EdgeInsets.all(16),
                        itemCount: messages.length,
                        itemBuilder: (context, index) {
                          return _buildMessageBubble(context, messages[index]);
                        },
                      ),
          ),
          _buildInputArea(),
        ],
      ),
    );
  }

  Widget _buildMessageBubble(BuildContext context, Message message) {
    final isMe = message.senderId == widget.userId;
    // TODO: 获取当前用户ID进行比较
    final isSelf = !isMe;

    return Align(
      alignment: isSelf ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isSelf
              ? Theme.of(context).colorScheme.primary
              : Colors.grey.shade200,
          borderRadius: BorderRadius.circular(16),
        ),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.7,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              message.content,
              style: TextStyle(
                color: isSelf ? Colors.white : Colors.black87,
              ),
            ),
            const SizedBox(height: 4),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  _formatTime(message.createTime),
                  style: TextStyle(
                    fontSize: 10,
                    color: isSelf ? Colors.white70 : Colors.black54,
                  ),
                ),
                if (!message.isSent) ...[
                  const SizedBox(width: 4),
                  const SizedBox(
                    width: 12,
                    height: 12,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInputArea() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.shade200,
            blurRadius: 4,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _messageController,
              decoration: const InputDecoration(
                hintText: '输入消息...',
                border: OutlineInputBorder(),
                contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              ),
              maxLines: null,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => _sendMessage(),
            ),
          ),
          const SizedBox(width: 8),
          IconButton(
            onPressed: _sendMessage,
            icon: const Icon(Icons.send),
            color: Theme.of(context).colorScheme.primary,
          ),
        ],
      ),
    );
  }

  String _formatTime(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inMinutes < 1) {
      return '刚刚';
    } else if (difference.inHours < 1) {
      return '${difference.inMinutes}分钟前';
    } else if (difference.inDays < 1) {
      return '${difference.inHours}小时前';
    } else {
      return '${dateTime.month}/${dateTime.day} ${dateTime.hour}:${dateTime.minute.toString().padLeft(2, '0')}';
    }
  }
}
```

- [ ] **Step 2: 更新main.dart导航**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/auth_provider.dart';
import 'providers/chat_provider.dart';
import 'pages/login_page.dart';
import 'pages/chat_list_page.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
      ],
      child: MaterialApp(
        title: 'Vibe IM',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
          useMaterial3: true,
        ),
        home: Consumer<AuthProvider>(
          builder: (context, authProvider, _) {
            return authProvider.isLoggedIn
                ? const ChatListPage()
                : const LoginPage();
          },
        ),
      ),
    );
  }
}
```

- [ ] **Step 3: 提交聊天页面**

```bash
git add lib/pages/chat_page.dart lib/main.dart
git commit -m "feat: implement chat page with message input and display"
```

---

## 第五部分：集成测试和验证

### Task 23: 编写集成测试

**Files:**
- Create: `backend/src/test/java/com/vibe/integration/ChatFlowIntegrationTest.java`

- [ ] **Step 1: 创建完整流程集成测试**

```java
package com.vibe.im.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.request.SendMessageRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.dto.response.PageResponse;
import com.vibe.im.dto.response.UserResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatFlowIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>(
        DockerImageName.parse("mysql:8.0")
    );

    @Container
    @ServiceConnection
    static final RedisContainer<?> redis = new RedisContainer<>(
        DockerImageName.parse("redis:7-alpine")
    );

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static String userASessionId;
    private static String userBSessionId;
    private static Long userAId;
    private static Long userBId;

    @Test
    @Order(1)
    void registerUsers() {
        RegisterRequest userA = new RegisterRequest();
        userA.setUsername("userA");
        userA.setPassword("password123");
        userA.setNickname("张三");

        ResponseEntity<UserResponse> responseA = restTemplate.postForEntity(
            "/api/auth/register",
            userA,
            UserResponse.class
        );

        assertThat(responseA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseA.getBody().getUsername()).isEqualTo("userA");

        RegisterRequest userB = new RegisterRequest();
        userB.setUsername("userB");
        userB.setPassword("password123");
        userB.setNickname("李四");

        ResponseEntity<UserResponse> responseB = restTemplate.postForEntity(
            "/api/auth/register",
            userB,
            UserResponse.class
        );

        assertThat(responseB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseB.getBody().getUsername()).isEqualTo("userB");
    }

    @Test
    @Order(2)
    void loginUserA() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("userA");
        loginRequest.setPassword("password123");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse loginResponse = response.getBody();
        assertThat(loginResponse.getSessionId()).isNotEmpty();
        assertThat(loginResponse.getUsername()).isEqualTo("userA");

        userASessionId = loginResponse.getSessionId();
        userAId = loginResponse.getUserId();
    }

    @Test
    @Order(3)
    void loginUserB() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("userB");
        loginRequest.setPassword("password123");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse loginResponse = response.getBody();
        assertThat(loginResponse.getSessionId()).isNotEmpty();

        userBSessionId = loginResponse.getSessionId();
        userBId = loginResponse.getUserId();
    }

    @Test
    @Order(4)
    void sendMessageFromUserAToUserB() {
        SendMessageRequest messageRequest = new SendMessageRequest();
        messageRequest.setReceiverId(userBId);
        messageRequest.setContent("你好，李四！");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Session-Id", userASessionId);
        headers.set("User-Id", String.valueOf(userAId));

        HttpEntity<SendMessageRequest> entity = new HttpEntity<>(messageRequest, headers);

        ResponseEntity<MessageResponse> response = restTemplate.postForEntity(
            "/api/chat/send",
            entity,
            MessageResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MessageResponse messageResponse = response.getBody();
        assertThat(messageResponse.getMessageId()).isNotEmpty();
        assertThat(messageResponse.getContent()).isEqualTo("你好，李四！");
        assertThat(messageResponse.getSenderId()).isEqualTo(userAId);
        assertThat(messageResponse.getReceiverId()).isEqualTo(userBId);
    }

    @Test
    @Order(5)
    void getChatHistoryForUserB() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Session-Id", userBSessionId);
        headers.set("User-Id", String.valueOf(userBId));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PageResponse> response = restTemplate.exchange(
                "/api/chat/messages?friendId=" + userAId + "&page=0&size=10",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<PageResponse<MessageResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<MessageResponse> pageResponse = response.getBody();
        assertThat(pageResponse.getContent()).isNotEmpty();
        assertThat(pageResponse.getContent().get(0).getSenderName()).isEqualTo("张三");
    }

    @Test
    @Order(6)
    void logoutUserA() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Session-Id", userASessionId);
        headers.set("User-Id", String.valueOf(userAId));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/logout",
            entity,
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 验证Session已失效
        headers.set("Session-Id", userASessionId);
        ResponseEntity<?> failedResponse = restTemplate.postForEntity(
            "/api/chat/send",
            entity,
            Void.class
        );

        assertThat(failedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

- [ ] **Step 2: 更新pom.xml添加TestContainers依赖**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>redis</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 3: 运行集成测试**

```bash
mvn test -Dtest=ChatFlowIntegrationTest
```
Expected: All tests PASS

- [ ] **Step 4: 提交集成测试**

```bash
git add src/test/java/com/vibe/integration/ pom.xml
git commit -m "test: add integration tests for complete chat flow"
```

---

### Task 24: 创建Docker Compose文件

**Files:**
- Create: `backend/docker-compose.yml`
- Create: `backend/Dockerfile`

- [ ] **Step 1: 创建docker-compose.yml**

```yaml
version: '3.8'

services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/vibe_im?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      - mysql
      - redis
    networks:
      - vibe-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: vibe_im
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - vibe-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot123"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - vibe-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
  redis-data:

networks:
  vibe-network:
    driver: bridge
```

- [ ] **Step 2: 创建Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/vibe-im-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: 提交Docker配置**

```bash
cd /Users/yiqi/IdeaProjects/vibeCodingDemo/backend
git add docker-compose.yml Dockerfile
git commit -m "feat: add Docker configuration for deployment"
```

---

### Task 25: 创建README文档

**Files:**
- Create: `backend/README.md`

- [ ] **Step 1: 创建README文档**

```markdown
# Vibe IM Backend

即时通信后端服务 - 第一阶段

## 技术栈

- Java 21
- Spring Boot 3.2.0
- Spring Data JPA
- Spring WebSocket
- MySQL 8.0
- Redis 7.x

## 快速开始

### 前置要求

- JDK 21+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x

### 使用Docker Compose启动

```bash
# 构建项目
mvn clean package

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f backend
```

### 本地开发

```bash
# 启动MySQL和Redis
docker-compose up mysql redis

# 启动应用
mvn spring-boot:run
```

## API文档

### 认证接口

#### 注册用户
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "nickname": "测试用户"
}
```

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}

Response:
{
  "sessionId": "uuid",
  "userId": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "avatar": null,
  "status": "ONLINE",
  "createTime": "2026-03-31T10:00:00"
}
```

#### 用户登出
```
POST /api/auth/logout
Headers:
  Session-Id: <sessionId>
```

### 聊天接口

#### 发送消息
```
POST /api/chat/send
Headers:
  Session-Id: <sessionId>
  User-Id: <userId>
Content-Type: application/json

{
  "receiverId": 2,
  "content": "你好！",
  "messageType": "TEXT"
}
```

#### 获取历史消息
```
GET /api/chat/messages?friendId=2&page=0&size=20
Headers:
  Session-Id: <sessionId>
  User-Id: <userId>
```

### WebSocket连接

```
ws://localhost:8080/api/ws?sessionId=<sessionId>

心跳:
Client -> Server: {"type":"PING"}
Server -> Client: {"type":"PONG"}

新消息:
Server -> Client: {
  "messageId": "xxx",
  "senderId": 1,
  "receiverId": 2,
  "content": "你好！",
  "messageType": "TEXT",
  "status": "DELIVERED",
  "createTime": "2026-03-31T10:00:00"
}
```

## 运行测试

```bash
# 单元测试
mvn test

# 集成测试
mvn verify
```

## 项目结构

```
src/main/java/com/vibe/im/
├── config/          # 配置类
├── controller/      # REST控制器
├── service/         # 业务逻辑
├── repository/      # 数据访问层
├── entity/         # JPA实体
├── dto/            # 数据传输对象
├── websocket/      # WebSocket处理
├── exception/      # 异常处理
└── util/           # 工具类
```

## 下一步

第二阶段将实现：
- 消息可靠投递机制
- 高并发WebSocket优化
- 消息存储与检索（Elasticsearch）
```

- [ ] **Step 2: 提交README**

```bash
git add README.md
git commit -m "docs: add README with API documentation"
```

---

## 总结

本实施计划涵盖以下内容：

### 第一部分：项目初始化与环境搭建
- Task 1: 创建后端Maven项目结构
- Task 2: 创建枚举和异常类
- Task 3: 创建Redis配置和Snowflake ID生成器

### 第二部分：用户认证与授权系统
- Task 4: 创建User实体和Repository
- Task 5: 创建DTO类
- Task 6: 实现AuthService
- Task 7: 实现AuthController

### 第三部分：基础聊天功能实现
- Task 8: 创建Message实体和Repository
- Task 9: 创建聊天相关DTO
- Task 10: 实现ChatService
- Task 11: 实现ChatController
- Task 12: 实现ConnectionManager
- Task 13: 实现WebSocketHandler和配置
- Task 14: 集成WebSocket消息推送

### 第四部分：Flutter前端实现
- Task 15: 创建Flutter项目基础结构
- Task 16: 创建API服务
- Task 17: 创建WebSocket服务
- Task 18: 创建AuthProvider
- Task 19: 创建ChatProvider
- Task 20: 创建登录页面
- Task 21: 创建聊天列表页面
- Task 22: 创建聊天页面

### 第五部分：集成测试和验证
- Task 23: 编写集成测试
- Task 24: 创建Docker Compose文件
- Task 25: 创建README文档

**总计: 25个任务，涵盖后端、前端、测试和部署**
