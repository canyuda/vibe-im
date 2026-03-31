package com.vibe.im.repository;

import com.vibe.im.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 消息数据访问接口
 * 提供消息相关的数据库操作方法
 *
 * <p>主要功能：
 * <ul>
 *   <li>查询两个用户之间的聊天记录</li>
 *   <li>根据发送者和接收者查询消息</li>
 *   <li>根据消息状态查询消息</li>
 * </ul>
 *
 * <p>线程安全说明：
 * <ul>
 *   <li>Repository实例由Spring容器管理，线程安全</li>
 *   <li>所有方法调用都在事务上下文中执行</li>
 *   <li>底层使用Hibernate Session，保证内存一致性</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查询两个用户之间的聊天记录
     * 包括用户A发送给用户B和用户B发送给用户A的消息
     *
     * @param user1Id 用户1的ID
     * @param user2Id 用户2的ID
     * @param pageable 分页参数
     * @return 分页的消息列表
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :user1Id AND m.receiverId = :user2Id) OR " +
           "(m.senderId = :user2Id AND m.receiverId = :user1Id) " +
           "ORDER BY m.createTime DESC")
    Page<Message> findChatMessages(
        @Param("user1Id") Long user1Id,
        @Param("user2Id") Long user2Id,
        Pageable pageable
    );

    /**
     * 根据发送者ID和接收者ID查询消息
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param pageable 分页参数
     * @return 分页的消息列表
     */
    Page<Message> findBySenderIdAndReceiverId(
        Long senderId,
        Long receiverId,
        Pageable pageable
    );

    /**
     * 根据消息状态查询消息
     *
     * @param status 消息状态
     * @param pageable 分页参数
     * @return 分页的消息列表
     */
    Page<Message> findByStatus(
        com.vibe.im.entity.enums.MessageStatus status,
        Pageable pageable
    );

    /**
     * 根据接收者ID和消息状态查询消息
     * 用于查找离线消息
     *
     * @param receiverId 接收者ID
     * @param status 消息状态
     * @return 消息列表
     */
    java.util.List<Message> findByReceiverIdAndStatus(
        Long receiverId,
        com.vibe.im.entity.enums.MessageStatus status
    );
}
