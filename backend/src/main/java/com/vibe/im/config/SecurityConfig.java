package com.vibe.im.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置类
 * 配置密码加密等安全相关功能
 *
 * @author Claude
 * @since 1.0.0
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置BCrypt密码编码器Bean
     * 用于用户密码的加密和验证
     *
     * <p>BCrypt算法特点：
     * <ul>
     *   <li>自动加盐：每次加密结果不同</li>
     *   <li>计算成本可调：默认10，推荐范围4-31</li>
     *   <li>抗彩虹表攻击：即使数据库泄露也难以破解</li>
     * </ul>
     *
     * @return BCryptPasswordEncoder实例，强度为10
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
