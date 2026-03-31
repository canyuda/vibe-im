package com.vibe.im.dto.request;

import com.vibe.im.entity.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息请求DTO
 *
 * <p>用于接收发送消息请求的数据传输对象，包含接收者ID、消息内容和消息类型。
 * 所有字段均使用Jakarta Validation注解进行参数校验。
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息内容，最多1000个字符
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容长度不能超过1000个字符")
    private String content;

    /**
     * 消息类型，默认为文本消息
     */
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
}
