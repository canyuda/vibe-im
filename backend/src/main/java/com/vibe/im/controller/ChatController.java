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

/**
 * 聊天控制器
 * 提供消息发送和聊天历史查询的REST API
 *
 * <p>端点说明：
 * <ul>
 *   <li>POST /chat/send - 发送消息</li>
 *   <li>GET /chat/messages - 查询聊天记录</li>
 * </ul>
 *
 * <p>认证说明：
 * <ul>
 *   <li>所有端点都需要在请求头中携带Session-Id进行身份验证</li>
 *   <li>会话验证通过AuthService.getUserIdBySession()完成</li>
 *   <li>会话无效时抛出AUTHENTICATION_FAILED异常</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    /**
     * 发送消息
     *
     * <p>请求示例：
     * <pre>
     * POST /chat/send
     * Session-Id: 550e8400-e29b-41d4-a716-446655440000
     * Content-Type: application/json
     * {
     *   "receiverId": 2,
     *   "content": "你好",
     *   "messageType": "TEXT"
     * }
     * </pre>
     *
     * <p>响应示例（200 OK）：
     * <pre>
     * {
     *   "id": 1,
     *   "senderId": 1,
     *   "receiverId": 2,
     *   "content": "你好",
     *   "messageType": "TEXT",
     *   "status": "DELIVERED",
     *   "sendTime": "2026-03-30T10:00:00",
     *   "createTime": "2026-03-30T10:00:00",
     *   "updateTime": "2026-03-30T10:00:00",
     *   "senderName": "user1"
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
     * <p>响应示例（400 Bad Request）：
     * <pre>
     * {
     *   "code": 400,
     *   "message": "参数错误",
     *   "timestamp": "2026-03-30T10:00:00Z"
     * }
     * </pre>
     *
     * @param sessionId 会话ID（必填）
     * @param request 发送消息请求
     * @return 消息响应DTO
     * @throws BusinessException 当会话无效或参数错误时抛出
     */
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader(value = "Session-Id") String sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("收到发送消息请求，会话ID: {}, 接收者ID: {}", sessionId, request.getReceiverId());

        // 验证会话
        Long userId = authService.getUserIdBySession(sessionId);
        if (userId == null) {
            log.warn("发送消息失败，会话验证失败，会话ID: {}", sessionId);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 发送消息
        MessageResponse response = chatService.sendMessage(userId, request);
        log.info("消息发送成功，消息ID: {}, 发送者ID: {}, 接收者ID: {}",
            response.getId(), response.getSenderId(), response.getReceiverId());

        return ResponseEntity.ok(response);
    }

    /**
     * 查询聊天记录
     *
     * <p>请求示例：
     * <pre>
     * GET /chat/messages?friendId=2&page=0&size=20
     * Session-Id: 550e8400-e29b-41d4-a716-446655440000
     * </pre>
     *
     * <p>响应示例（200 OK）：
     * <pre>
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "senderId": 1,
     *       "receiverId": 2,
     *       "content": "你好",
     *       "messageType": "TEXT",
     *       "status": "DELIVERED",
     *       "sendTime": "2026-03-30T10:00:00",
     *       "createTime": "2026-03-30T10:00:00",
     *       "updateTime": "2026-03-30T10:00:00",
     *       "senderName": "user1"
     *     }
     *   ],
     *   "currentPage": 0,
     *   "totalPages": 1,
     *   "totalElements": 1,
     *   "pageSize": 20
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
     * <p>说明：
     * <ul>
     *   <li>消息按创建时间正序排序（最早的在前）</li>
     *   <li>page从0开始</li>
     *   <li>size默认为20</li>
     * </ul>
     *
     * @param sessionId 会话ID（必填）
     * @param friendId 好友用户ID（必填）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认20）
     * @return 分页的消息响应列表
     * @throws BusinessException 当会话无效时抛出
     */
    @GetMapping("/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getChatMessages(
            @RequestHeader(value = "Session-Id") String sessionId,
            @RequestParam Long friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("收到查询聊天记录请求，会话ID: {}, 好友ID: {}, 页码: {}, 每页大小: {}",
            sessionId, friendId, page, size);

        // 验证会话
        Long userId = authService.getUserIdBySession(sessionId);
        if (userId == null) {
            log.warn("查询聊天记录失败，会话验证失败，会话ID: {}", sessionId);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 查询聊天记录
        PageResponse<MessageResponse> response = chatService.getChatMessages(userId, friendId, page, size);
        log.info("查询聊天记录成功，用户ID: {}, 好友ID: {}, 总记录数: {}",
            userId, friendId, response.totalElements());

        return ResponseEntity.ok(response);
    }
}
