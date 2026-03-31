# IM即时通信产品 第一阶段设计规范

**文档版本**: v1.0
**创建日期**: 2026-03-30
**阶段**: 第一阶段（项目初始化 + 认证系统 + 基础聊天）

---

## 文档概述

本规范定义了IM即时通信产品第一阶段的设计方案，包括系统架构、数据模型、核心流程、错误处理、安全性和测试策略。

**技术栈**: Java 21 + Spring Boot 3.2 + MySQL 8.0 + Redis 7.x + Flutter 3.x

**架构模式**: 单体应用 + 内嵌模块

---

## 一、系统架构设计

### 1.1 整体架构

```
客户端层 (Flutter)
    ↓ HTTP/WebSocket
应用层 (Spring Boot)
    ├── Controller层
    ├── Service层
    └── WebSocket Handler
    ↓
存储层
    ├── MySQL 8.0
    └── Redis 7.x
```

### 1.2 核心模块划分

| 模块 | 职责 | 主要类 |
|------|------|--------|
| 认证模块 | 用户注册、登录、登出、Session管理 | AuthController, AuthService |
| 用户模块 | 用户信息查询、更新、状态管理 | UserController, UserService |
| 聊天模块 | 消息发送、历史查询、消息状态管理 | ChatController, ChatService |
| WebSocket模块 | 连接管理、消息推送、心跳检测 | WebSocketHandler, ConnectionManager |

---

## 二、数据模型设计

### 2.1 用户实体 (User)

```java
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;  // BCrypt加密

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;  // ONLINE, OFFLINE

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
```

### 2.2 消息实体 (Message)

```java
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;  // Snowflake全局唯一ID

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;  // TEXT, IMAGE, FILE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;  // SENDING, SENT, DELIVERED

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
```

### 2.3 枚举定义

```java
public enum UserStatus { ONLINE, OFFLINE }
public enum MessageType { TEXT, IMAGE, FILE }
public enum MessageStatus { SENDING, SENT, DELIVERED }
```

### 2.4 Redis数据结构

| Key | 类型 | 说明 | 过期时间 |
|-----|------|------|----------|
| `session:{sessionId}` | Hash | Session数据 | 7天 |
| `user:online:{userId}` | String | 用户在线状态 | 30分钟（心跳续期） |
| `user:socket:{userId}` | String | WebSocket连接ID | 连接断开时删除 |

---

## 三、API接口规范

### 3.1 认证接口

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | /api/auth/register | 用户注册 | `{username, password, nickname}` | `{userId, username, nickname, avatar, createTime}` |
| POST | /api/auth/login | 用户登录 | `{username, password}` | `{sessionId, userId, username, nickname, avatar}` |
| POST | /api/auth/logout | 用户登出 | - | `{success: true}` |
| GET | /api/auth/me | 获取当前用户信息 | Header: Session-Id | 用户信息 |

### 3.2 用户接口

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| GET | /api/user/{id} | 获取用户信息 | - | 用户信息 |
| PUT | /api/user/{id} | 更新用户信息 | `{nickname, avatar}` | 更新后信息 |
| GET | /api/user/{id}/status | 获取用户在线状态 | - | `{status: ONLINE/OFFLINE}` |

### 3.3 聊天接口

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | /api/chat/send | 发送消息 | `{receiverId, content, messageType}` | `{messageId, status}` |
| GET | /api/chat/messages | 获取历史消息 | `?friendId={id}&page={n}&size={20}` | 分页消息列表 |

### 3.4 WebSocket接口

| 事件方向 | 事件类型 | 说明 | 数据 |
|---------|---------|------|------|
| 客户端→服务端 | PING | 心跳 | - |
| 服务端→客户端 | PONG | 心跳响应 | - |
| 服务端→客户端 | MESSAGE | 新消息 | `{messageId, senderId, content, messageType, createTime}` |
| 服务端→客户端 | MESSAGE_STATUS | 消息状态更新 | `{messageId, status}` |

---

## 四、核心流程

### 4.1 用户登录流程

1. 客户端发送用户名/密码到 `/api/auth/login`
2. 服务端验证用户名和密码（BCrypt）
3. 验证成功后，在Redis创建Session
4. 更新用户状态为ONLINE
5. 返回SessionID和用户信息

### 4.2 WebSocket连接流程

1. 客户端发起WebSocket连接（携带SessionID）
2. 服务端验证Session有效性
3. 验证成功后，注册连接到ConnectionManager
4. 更新Redis在线状态
5. 开始心跳检测（30秒超时）

### 4.3 消息发送流程

1. 客户端通过REST API发送消息
2. 服务端生成全局messageId（Snowflake）
3. 持久化消息到MySQL（status=SENDING）
4. 检查接收者在线状态（Redis）
5. 若在线，通过WebSocket推送；若离线，消息存储待推送
6. 更新消息状态为SENT
7. 返回成功响应

### 4.4 历史消息查询流程

1. 客户端请求历史消息（分页）
2. 服务端从MySQL查询消息列表
3. 按时间正序排列
4. 返回分页结果

---

## 五、错误处理

### 5.1 异常体系

```java
BusinessException (基类)
├── AuthenticationException
├── UserNotFoundException
├── MessageNotFoundException
└── InvalidMessageException
```

### 5.2 错误响应格式

```json
{
  "code": 401,
  "message": "认证失败",
  "path": "/api/auth/login",
  "timestamp": "2026-03-30T10:00:00"
}
```

### 5.3 错误码

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 认证失败 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 六、安全性设计

### 6.1 密码安全

- 使用BCrypt算法加密（强度10）
- 密码长度要求6-20字符
- 密码存储永不解密，仅使用checkpw验证

### 6.2 Session安全

- Session存储于Redis，7天过期
- 通过HTTPS传输
- 每次请求验证Session有效性
- 登出立即删除Session

### 6.3 WebSocket安全

- 握手时验证Session
- 验证消息发送者与Session一致
- 30秒心跳超时自动断开
- 单用户限制1个活跃连接

---

## 七、测试策略

### 7.1 测试覆盖率目标

| 测试类型 | 覆盖率目标 |
|---------|-----------|
| 单元测试 | > 80% |
| 集成测试 | > 60% |
| E2E测试 | 覆盖核心流程 |

### 7.2 测试工具

| 测试类型 | 工具 |
|---------|------|
| 单元测试 | JUnit 5 + Mockito |
| 集成测试 | Spring Boot Test + TestContainers |
| API测试 | Postman/REST Assured |
| 性能测试 | JMeter/Gatling |

---

## 八、部署配置

### 8.1 技术栈版本

| 组件 | 版本 |
|------|------|
| JDK | 21 |
| Spring Boot | 3.2.0 |
| MySQL | 8.0+ |
| Redis | 7.x |
| Flutter | 3.24.0 |

### 8.2 端口配置

| 服务 | 端口 |
|------|------|
| HTTP API | 8080 |
| WebSocket | 8080 |
| MySQL | 3306 |
| Redis | 6379 |

### 8.3 Docker部署

使用docker-compose一键部署：
- backend: Spring Boot应用
- mysql: MySQL 8.0
- redis: Redis 7.x

---

## 九、项目结构

```
vibeCodingDemo/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/vibe/im/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── WebSocketConfig.java
│   │   │   │   │   └── RedisConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   └── ChatController.java
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   ├── websocket/
│   │   │   │   └── exception/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── lib/
│   │   ├── pages/
│   │   ├── widgets/
│   │   ├── models/
│   │   ├── services/
│   │   └── providers/
│   └── pubspec.yaml
└── docs/
```

---

## 十、实施顺序

1. **项目初始化与环境搭建**
   - 创建Maven项目和Flutter项目
   - 配置依赖和基础配置
   - 设置Docker环境

2. **用户认证与授权系统**
   - 实现用户实体和Repository
   - 实现AuthService（注册、登录、登出）
   - 实现AuthController
   - 集成Redis Session

3. **基础聊天功能实现**
   - 实现消息实体和Repository
   - 实现ChatService（发送消息、历史查询）
   - 实现ChatController
   - 实现WebSocket连接管理
   - 实现消息推送功能

---

**文档状态**: 待审核
**下一步**: 创建实施计划
