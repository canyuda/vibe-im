package com.vibe.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.request.SendMessageRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.dto.response.PageResponse;
import com.vibe.im.dto.response.UserResponse;
import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.entity.enums.MessageType;
import com.vibe.im.entity.enums.UserStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.redis.testcontainers.RedisContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 聊天流程集成测试
 *
 * <p>使用TestContainers测试完整的用户注册、登录、消息发送和登出流程
 *
 * <p>测试流程：
 * <ol>
 *   <li>注册两个用户（张三、李四）</li>
 *   <li>用户A（张三）登录</li>
 *   <li>用户B（李四）登录</li>
 *   <li>用户A向用户B发送消息</li>
 *   <li>用户B查询聊天历史</li>
 *   <li>用户A登出</li>
 * </ol>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatFlowIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("vibe_im")
            .withUsername("root")
            .withPassword("root");

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Static fields to store session IDs and user IDs across tests
    static String userASessionId;
    static String userBSessionId;
    static Long userAId;
    static Long userBId;

    private static final String API_BASE = "/api";

    @BeforeAll
    static void beforeAll() {
        // Wait for containers to be ready
        assertTrue(mysqlContainer.isCreated());
        assertTrue(redisContainer.isCreated());
    }

    /**
     * 测试用户注册
     */
    @Test
    @Order(1)
    void registerUsers() {
        // Register user A (张三)
        RegisterRequest registerRequestA = RegisterRequest.builder()
                .username("zhangsan")
                .password("password123")
                .nickname("张三")
                .build();

        ResponseEntity<UserResponse> responseA = restTemplate.postForEntity(
                API_BASE + "/auth/register",
                registerRequestA,
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, responseA.getStatusCode());
        assertNotNull(responseA.getBody());
        assertEquals("zhangsan", responseA.getBody().getUsername());
        assertEquals("张三", responseA.getBody().getNickname());
        assertEquals(UserStatus.OFFLINE, responseA.getBody().getStatus());
        userAId = responseA.getBody().getId();
        assertNotNull(userAId);

        // Register user B (李四)
        RegisterRequest registerRequestB = RegisterRequest.builder()
                .username("lisi")
                .password("password456")
                .nickname("李四")
                .build();

        ResponseEntity<UserResponse> responseB = restTemplate.postForEntity(
                API_BASE + "/auth/register",
                registerRequestB,
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, responseB.getStatusCode());
        assertNotNull(responseB.getBody());
        assertEquals("lisi", responseB.getBody().getUsername());
        assertEquals("李四", responseB.getBody().getNickname());
        assertEquals(UserStatus.OFFLINE, responseB.getBody().getStatus());
        userBId = responseB.getBody().getId();
        assertNotNull(userBId);
    }

    /**
     * 测试用户A登录
     */
    @Test
    @Order(2)
    void loginUserA() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("zhangsan")
                .password("password123")
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                API_BASE + "/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getSessionId());
        assertEquals("zhangsan", response.getBody().getUsername());
        assertEquals("张三", response.getBody().getNickname());
        assertEquals(UserStatus.ONLINE, response.getBody().getStatus());

        userASessionId = response.getBody().getSessionId();
        assertNotNull(userASessionId);
    }

    /**
     * 测试用户B登录
     */
    @Test
    @Order(3)
    void loginUserB() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("lisi")
                .password("password456")
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                API_BASE + "/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getSessionId());
        assertEquals("lisi", response.getBody().getUsername());
        assertEquals("李四", response.getBody().getNickname());
        assertEquals(UserStatus.ONLINE, response.getBody().getStatus());

        userBSessionId = response.getBody().getSessionId();
        assertNotNull(userBSessionId);
    }

    /**
     * 测试用户A向用户B发送消息
     */
    @Test
    @Order(4)
    void sendMessageFromUserAToUserB() {
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .receiverId(userBId)
                .content("你好，李四！")
                .messageType(MessageType.TEXT)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Session-Id", userASessionId);
        headers.set("User-Id", String.valueOf(userAId));

        HttpEntity<SendMessageRequest> requestEntity = new HttpEntity<>(sendMessageRequest, headers);

        ResponseEntity<MessageResponse> response = restTemplate.postForEntity(
                API_BASE + "/chat/send",
                requestEntity,
                MessageResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("你好，李四！", response.getBody().getContent());
        assertEquals(userAId, response.getBody().getSenderId());
        assertEquals(userBId, response.getBody().getReceiverId());
        assertEquals(MessageType.TEXT, response.getBody().getMessageType());
        assertEquals(MessageStatus.DELIVERED, response.getBody().getStatus());
        assertEquals("张三", response.getBody().getSenderName());
    }

    /**
     * 测试用户B查询与用户A的聊天历史
     */
    @Test
    @Order(5)
    void getChatHistoryForUserB() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Session-Id", userBSessionId);

        String url = API_BASE + "/chat/messages?friendId=" + userAId + "&page=0&size=20";
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<PageResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                PageResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().content());
        assertTrue(response.getBody().content().size() > 0);

        // Verify message content
        @SuppressWarnings("unchecked")
        List<MessageResponse> messages = (List<MessageResponse>) response.getBody().content();
        MessageResponse message = messages.get(0);
        assertNotNull(message);
        assertEquals("你好，李四！", message.getContent());
        assertEquals(userAId, message.getSenderId());
        assertEquals(userBId, message.getReceiverId());
        assertEquals("张三", message.getSenderName());
    }

    /**
     * 测试用户A登出
     */
    @Test
    @Order(6)
    void logoutUserA() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Session-Id", userASessionId);
        headers.set("User-Id", String.valueOf(userAId));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                API_BASE + "/auth/logout",
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
