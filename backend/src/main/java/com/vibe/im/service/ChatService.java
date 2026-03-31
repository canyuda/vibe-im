package com.vibe.im.service;

import com.vibe.im.dto.request.SendMessageRequest;
import com.vibe.im.dto.response.MessageResponse;
import com.vibe.im.dto.response.PageResponse;
import com.vibe.im.entity.Message;
import com.vibe.im.entity.User;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 聊天服务实现类
 * 提供消息发送、消息历史查询等功能
 *
 * <p>核心功能：
 * <ul>
 *   <li>发送消息：验证接收者、生成消息ID、更新消息状态</li>
 *   <li>查询聊天记录：支持分页查询，按时间顺序返回</li>
 * </ul>
 *
 * <p>线程安全说明：
 * <ul>
 *   <li>使用@Transactional保证事务的原子性</li>
 *   <li>消息状态更新在同一个事务中完成，保证一致性</li>
 *   <li>分页查询使用Spring Data JPA，保证查询结果的稳定性</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 发送消息
     *
     * <p>处理流程：
     * <ol>
     *   <li>验证接收者是否存在</li>
     *   <li>验证不能发送给自己</li>
 *   <li>生成消息ID（使用Snowflake算法）</li>
 *   <li>创建消息实体，初始状态为SENDING</li>
 *   <li>保存消息到数据库</li>
 *   <li>根据接收者在线状态更新消息状态（在线为DELIVERED，离线为SENT）</li>
 *   <li>TODO: 通过WebSocket推送消息给接收者</li>
 * </ol>
     *
     * @param senderId 发送者ID
     * @param request 发送消息请求
     * @return 消息响应DTO
     * @throws BusinessException 当接收者不存在或发送给自己时抛出异常
     */
    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        log.debug("开始发送消息，senderId: {}, receiverId: {}", senderId, request.getReceiverId());

        // 验证接收者是否存在
        User receiver = userRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 验证不能发送给自己
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        // 生成消息ID
        String messageId = snowflakeIdGenerator.generateId();
        log.debug("生成消息ID: {}", messageId);

        // 创建消息实体
        Message message = Message.builder()
            .senderId(senderId)
            .receiverId(request.getReceiverId())
            .content(request.getContent())
            .messageType(request.getMessageType())
            .status(MessageStatus.SENDING)
            .sendTime(LocalDateTime.now())
            .build();

        // 保存消息
        Message savedMessage = messageRepository.save(message);
        log.debug("消息已保存，ID: {}", savedMessage.getId());

        // 根据接收者在线状态更新消息状态
        MessageStatus finalStatus = receiver.getStatus().name().equals("ONLINE")
            ? MessageStatus.DELIVERED
            : MessageStatus.SENT;
        savedMessage.setStatus(finalStatus);
        savedMessage.setSendTime(LocalDateTime.now());
        messageRepository.save(savedMessage);
        log.debug("消息状态已更新为: {}", finalStatus);

        // TODO: WebSocket推送消息给接收者
        // webSocketService.sendMessage(receiverId, savedMessage);

        return toMessageResponse(savedMessage, receiver.getUsername());
    }

    /**
     * 查询聊天记录
     *
     * <p>处理流程：
     * <ol>
     *   <li>创建分页参数，按创建时间倒序查询</li>
 *   <li>调用Repository查询两个用户之间的消息</li>
 *   <li>将消息实体转换为响应DTO</li>
 *   <li>按创建时间正序排序（最早的在前）</li>
 *   <li>封装为分页响应返回</li>
 * </ol>
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页的消息响应列表
     */
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getChatMessages(Long userId1, Long userId2, int page, int size) {
        log.debug("查询聊天记录，userId1: {}, userId2: {}, page: {}, size: {}", userId1, userId2, page, size);

        // 创建分页参数，按创建时间倒序查询
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 查询消息
        Page<Message> messagePage = messageRepository.findChatMessages(userId1, userId2, pageable);
        log.debug("查询到 {} 条消息", messagePage.getTotalElements());

        // 转换为响应DTO
        List<MessageResponse> responseList = messagePage.getContent().stream()
            .map(message -> {
                // 查询发送者信息
                User sender = userRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                return toMessageResponse(message, sender.getUsername());
            })
            // 按创建时间正序排序（最早的在前）
            .sorted(Comparator.comparing(MessageResponse::getCreateTime))
            .toList();

        return PageResponse.of(
            new org.springframework.data.domain.PageImpl<>(
                responseList,
                messagePage.getPageable(),
                messagePage.getTotalElements()
            )
        );
    }

    /**
     * 将消息实体转换为响应DTO
     *
     * @param message 消息实体
     * @param senderName 发送者用户名
     * @return 消息响应DTO
     */
    private MessageResponse toMessageResponse(Message message, String senderName) {
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
            .senderName(senderName)
            .build();
    }
}
