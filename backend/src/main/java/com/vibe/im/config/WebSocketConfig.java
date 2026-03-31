package com.vibe.im.config;

import com.vibe.im.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 配置WebSocket端点和处理器
 *
 * <p>核心功能：
 * <ul>
 *   <li>启用WebSocket支持</li>
 *   <li>注册WebSocket端点路径（/ws）</li>
 *   <li>配置WebSocket处理器</li>
 *   <li>允许跨域访问</li>
 * </ul>
 *
 * <p>线程安全说明：
 * <ul>
 *   <li>配置类由Spring容器管理，线程安全</li>
 *   <li>WebSocketHandler通过Spring注入，本身是线程安全的</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    /**
     * 注册WebSocket处理器
     *
     * <p>配置WebSocket端点：
     * <ul>
     *   <li>端点路径：/ws</li>
     *   <li>处理器：WebSocketHandler</li>
     *   <li>允许跨域：所有来源</li>
     * </ul>
     *
     * @param registry WebSocket处理器注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}
