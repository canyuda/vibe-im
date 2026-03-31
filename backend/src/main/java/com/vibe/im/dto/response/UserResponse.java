package com.vibe.im.dto.response;

import com.vibe.im.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 *
 * <p>用于返回用户基本信息的数据传输对象，字段与User实体对应。
 * 不包含敏感信息如密码，仅返回用户展示所需字段。
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
