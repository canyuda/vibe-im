package com.vibe.im.entity;

import com.vibe.im.entity.enums.MessageStatus;
import com.vibe.im.entity.enums.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 消息实体类
 * 用于存储IM系统中的消息记录
 *
 * <p>核心字段说明：
 * <ul>
 *   <li>id: 主键，使用数据库自增策略</li>
 *   <li>senderId: 发送者ID</li>
 *   <li>receiverId: 接收者ID</li>
 *   <li>content: 消息内容</li>
 *   <li>messageType: 消息类型（文本/图片/文件）</li>
 *   <li>status: 消息状态（发送中/已发送/已送达）</li>
 *   <li>sendTime: 消息发送时间</li>
 *   <li>createTime: 创建时间</li>
 *   <li>updateTime: 更新时间</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Entity
@Table(name = "message", indexes = {
    @Index(name = "idx_sender_receiver", columnList = "sender_id, receiver_id"),
    @Index(name = "idx_create_time", columnList = "create_time"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * 消息ID，主键，数据库自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * 接收者ID
     */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * 消息状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENDING;

    /**
     * 消息发送时间
     */
    @Column(name = "send_time")
    private LocalDateTime sendTime;

    /**
     * 创建时间，自动设置
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动更新
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
