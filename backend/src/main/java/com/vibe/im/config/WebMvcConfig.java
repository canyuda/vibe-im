package com.vibe.im.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置CORS（跨域资源共享）策略
 *
 * <p>CORS配置说明：
 * <ul>
 *   <li>允许所有来源访问（开发环境）</li>
 *   <li>允许所有HTTP方法</li>
 *   <li>允许所有请求头</li>
 *   <li>允许携带认证信息</li>
 * </ul>
 *
 * <p>生产环境建议：
 * <ul>
 *   <li>限制允许的来源域名</li>
 *   <li>仅允许必要的HTTP方法</li>
 *   <li>仅允许必要的请求头</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置CORS映射
     *
     * <p>允许前端（localhost:8081）访问后端API（localhost:8080）
     *
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 允许携带Cookie
                .maxAge(3600);  // 预检请求缓存1小时
    }
}
