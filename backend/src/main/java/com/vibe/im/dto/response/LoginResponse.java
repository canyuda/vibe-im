package com.vibe.im.dto.response;

import com.vibe.im.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 登录响应DTO
 *
 * <p>用于返回登录成功后的数据，继承UserResponse的所有字段，
 * 并额外包含会话ID用于后续的会话管理和身份验证。
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends UserResponse {

    /**
     * 会话ID，用于后续请求的身份验证
     */
    private String sessionId;
}
