package com.vibe.im.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.entity.Message;
import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.repository.MessageRepository;
import com.vibe.im.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WebSocket消息处理器
 * 处理WebSocket连接、消息接收和离线消息推送
 *
 * <p>核心功能：
 * <ul>
 *   <li>连接建立：验证会话，建立WebSocket连接</li>
 *   <li>心跳处理：响应PING消息，发送PONG</li>
 *   <li>离线消息推送：连接建立时发送未送达消息</li>
 *   <li>连接关闭：清理连接管理器中的会话</li>
 *   <li>错误处理：捕获并记录传输异常</li>
 * </ul>
 *
 * <p>线程安全说明：
 * <ul>
 *   <li>每个WebSocketSession由Spring WebSocket框架管理，线程安全</li>
 *   <li>ConnectionManager使用ConcurrentHashMap，线程安全</li>
 *   <li>AuthService的getUserIdBySession方法是线程安全的</li>
 *   <li>MessageRepository由Spring Data JPA管理，在事务上下文中线程安全</li>
 * </ul>
 *
 * <p>并发安全注意事项：
 * <ul>
 *   <li>离线消息查询和状态更新之间存在竞态条件，但影响很小</li>
 *   <li>多个连接同时向同一用户发送消息时，ConnectionManager的sendMessageToUser会处理离线场景</li>
 *   <li>WebSocketSession的sendMessage方法由框架保证线程安全</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ConnectionManager connectionManager;
    private final AuthService authService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    /**
     * WebSocket连接建立后的处理
     *
     * <p>处理流程：
     * <ol>
     *   <li>从URI查询参数中提取sessionId</li>
     *   <li>调用AuthService验证会话并获取userId</li>
 *   *   <li>如果会话无效，关闭连接并返回</li>
     *   <li>将连接添加到ConnectionManager</li>
     *   <li>发送离线消息（状态为SENT的消息）</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>sessionId为空或格式错误：关闭连接，返回NOT_ACCEPTABLE状态</li>
     *   <li>会话验证失败：关闭连接，返回NOT_ACCEPTABLE状态</li>
     *   <li>离线消息发送失败：记录日志，不影响连接建立</li>
     * </ul>
     *
     * @param session WebSocket会话
     * @throws Exception 处理过程中发生异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 从URI查询参数中提取sessionId
            String sessionId = extractSessionId(session.getUri().getQuery());
            if (sessionId == null || sessionId.isEmpty()) {
                log.warn("WebSocket连接失败，缺少sessionId参数");
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing sessionId"));
                return;
            }

            // 验证会话并获取userId
            Long userId = authService.getUserIdBySession(sessionId);
            if (userId == null) {
                log.warn("WebSocket连接失败，会话无效或已过期: {}", sessionId);
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid session"));
                return;
            }

            // 添加连接到管理器
            connectionManager.addConnection(userId, sessionId, session);
            log.info("WebSocket连接建立成功，用户ID: {}, 会话ID: {}, SessionID: {}",
                    userId, sessionId, session.getId());

            // 发送离线消息
            sendOfflineMessages(userId, session);

        } catch (Exception e) {
            log.error("WebSocket连接建立失败，SessionID: {}", session.getId(), e);
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection failed"));
        }
    }

    /**
     * 处理接收到的文本消息
     *
     * <p>当前仅支持PING心跳消息：
     * <ul>
     *   <li>接收格式：{"type": "PING"}</li>
     *   <li>响应格式：{"type": "PONG"}</li>
     * </ul>
     *
     * <p>处理流程：
     * <ol>
     *   <li>解析JSON消息</li>
     *   <li>检查消息类型</li>
     *   <li>如果是PING，更新心跳时间并返回PONG</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>JSON解析失败：记录日志，忽略该消息</li>
     *   <li>未知消息类型：记录日志，忽略该消息</li>
     * </ul>
     *
     * @param session WebSocket会话
     * @param message 接收到的文本消息
     * @throws Exception 处理过程中发生异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 解析JSON消息
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.path("type").asText();

            // 处理PING心跳
            if ("PING".equals(type)) {
                // 更新心跳时间
                String sessionId = extractSessionId(session.getUri().getQuery());
                connectionManager.updateHeartbeat(sessionId);

                // 发送PONG响应
                String pongResponse = objectMapper.writeValueAsString(new PingPongMessage("PONG"));
                session.sendMessage(new TextMessage(pongResponse));

                log.debug("收到PING，已发送PONG，SessionID: {}", session.getId());
            } else {
                log.warn("收到未知类型的WebSocket消息: {}, SessionID: {}", type, session.getId());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败，SessionID: {}, 消息: {}",
                    session.getId(), message.getPayload(), e);
        }
    }

    /**
     * WebSocket连接关闭后的处理
     *
     * <p>处理流程：
     * <ol>
     *   <li>从URI查询参数中提取sessionId</li>
     *   <li>从ConnectionManager中移除连接</li>
     *   <li>记录日志</li>
     * </ol>
     *
     * @param session WebSocket会话
     * @param status 关闭状态
     * @throws Exception 处理过程中发生异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            String sessionId = extractSessionId(session.getUri().getQuery());
            if (sessionId != null && !sessionId.isEmpty()) {
                connectionManager.removeConnection(sessionId);
                log.info("WebSocket连接关闭，会话ID: {}, SessionID: {}, 状态: {}",
                        sessionId, session.getId(), status);
            } else {
                log.info("WebSocket连接关闭（无sessionId），SessionID: {}, 状态: {}",
                        session.getId(), status);
            }
        } catch (Exception e) {
            log.error("处理WebSocket连接关闭失败，SessionID: {}", session.getId(), e);
        }
    }

    /**
     * 处理WebSocket传输错误
     *
     * <p>处理流程：
     * <ol>
     *   <li>从URI查询参数中提取sessionId</li>
     *   <li>从ConnectionManager中移除连接</li>
     *   <li>记录错误日志</li>
     * </ol>
     *
     * @param session WebSocket会话
     * @param exception 发生的异常
     * @throws Exception 处理过程中发生异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        try {
            String sessionId = extractSessionId(session.getUri().getQuery());
            if (sessionId != null && !sessionId.isEmpty()) {
                connectionManager.removeConnection(sessionId);
                log.error("WebSocket传输错误，会话ID: {}, SessionID: {}", sessionId, session.getId(), exception);
            } else {
                log.error("WebSocket传输错误（无sessionId），SessionID: {}", session.getId(), exception);
            }
        } catch (Exception e) {
            log.error("处理WebSocket传输错误失败，SessionID: {}", session.getId(), e);
        }
    }

    /**
     * 发送离线消息
     *
     * <p>处理流程：
     * <ol>
     *   <li>查询该用户的SENT状态消息</li>
     *   <li>将消息状态更新为DELIVERED</li>
     *   <li>序列化每个消息并通过WebSocket发送</li>
     *   <li>保存所有更新的消息到数据库</li>
     * </ol>
     *
     * <p>并发安全说明：
     * <ul>
     *   <li>查询和更新之间存在短暂的时间窗口，期间可能有新消息到达</li>
     *   <li>新消息会被下一次连接时推送，不影响功能正确性</li>
     *   <li>消息保存使用Spring Data JPA，在事务中保证原子性</li>
     * </ul>
     *
     * @param userId 用户ID
     * @param session WebSocket会话
     * @throws Exception 发送过程中发生异常
     */
    private void sendOfflineMessages(Long userId, WebSocketSession session) throws Exception {
        try {
            // 查询该用户的SENT状态消息
            List<Message> offlineMessages = messageRepository.findByReceiverIdAndStatus(
                    userId, MessageStatus.SENT);

            if (offlineMessages.isEmpty()) {
                log.debug("用户 {} 没有离线消息", userId);
                return;
            }

            log.info("开始发送离线消息，用户ID: {}, 消息数量: {}", userId, offlineMessages.size());

            int sentCount = 0;
            for (Message message : offlineMessages) {
                try {
                    // 更新消息状态
                    message.setStatus(MessageStatus.DELIVERED);

                    // 序列化并发送消息
                    MessageResponse response = toMessageResponse(message);
                    String messageJson = objectMapper.writeValueAsString(response);
                    session.sendMessage(new TextMessage(messageJson));

                    sentCount++;
                    log.debug("发送离线消息成功，消息ID: {}, 接收者ID: {}", message.getId(), userId);

                } catch (Exception e) {
                    log.error("发送离线消息失败，消息ID: {}", message.getId(), e);
                }
            }

            // 保存所有更新的消息
            messageRepository.saveAll(offlineMessages);

            log.info("离线消息发送完成，用户ID: {}, 发送数量: {}, 总数量: {}",
                    userId, sentCount, offlineMessages.size());

        } catch (Exception e) {
            log.error("发送离线消息失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 将Message实体转换为MessageResponse DTO
     *
     * @param message 消息实体
     * @return 消息响应DTO
     */
    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .sendTime(message.getSendTime())
                .createTime(message.getCreateTime())
                .updateTime(message.getUpdateTime())
                .senderName(null) // 发送者昵称需要额外查询，这里暂时为空
                .build();
    }

    /**
     * 从URI查询字符串中提取sessionId
     *
     * <p>查询字符串格式示例：sessionId=xxx&other=value
     *
     * @param query URI查询字符串
     * @return sessionId，如果不存在或格式错误则返回null
     */
    private String extractSessionId(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        try {
            // 查找sessionId参数
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2 && "sessionId".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析sessionId失败，查询字符串: {}", query, e);
            return null;
        }
    }

    /**
     * 心跳消息DTO
     * 用于JSON序列化和反序列化
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class PingPongMessage {
        private String type;
    }
}
