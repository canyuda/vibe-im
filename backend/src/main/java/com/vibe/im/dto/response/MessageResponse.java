package com.vibe.im.dto.response;

import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.entity.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息响应DTO
 *
 * <p>用于返回消息信息的数据传输对象，包含消息的所有字段以及发送者昵称。
 * 不包含敏感信息，仅返回前端展示所需字段。
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private MessageType messageType;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 发送时间
     */
    private java.time.LocalDateTime sendTime;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 发送者昵称（用于前端显示）
     */
    private String senderName;
}
