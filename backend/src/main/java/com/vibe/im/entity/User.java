package com.vibe.im.entity;

import com.vibe.im.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储IM系统中的用户信息
 *
 * <p>核心字段说明：
 * <ul>
 *   <li>id: 主键，使用数据库自增策略</li>
 *   <li>username: 用户名，全局唯一，用于登录</li>
 *   <li>password: 密码（明文存储，加密由Service层处理）</li>
 *   <li>nickname: 昵称，显示给其他用户</li>
 *   <li>avatar: 头像URL</li>
 *   <li>status: 用户状态（在线/离线）</li>
 *   <li>createTime: 创建时间</li>
 *   <li>updateTime: 更新时间</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Entity
@Table(name = "user", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户ID，主键，数据库自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，唯一标识
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（明文存储，由Service层负责加密）
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 用户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

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

    /**
     * 构造函数：仅创建用户时的必填字段
     *
     * @param username 用户名
     * @param password 密码
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.status = UserStatus.OFFLINE;
    }
}
