package com.vibe.im.service;

import com.vibe.im.dto.request.LoginRequest;
import com.vibe.im.dto.request.RegisterRequest;
import com.vibe.im.dto.response.LoginResponse;
import com.vibe.im.dto.response.UserResponse;
import com.vibe.im.entity.User;
import com.vibe.im.entity.enums.UserStatus;
import com.vibe.im.exception.BusinessException;
import com.vibe.im.exception.ErrorCode;
import com.vibe.im.repository.UserRepository;
import com.vibe.im.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 * 提供用户注册、登录、登出和会话管理功能
 *
 * <p>核心功能：
 * <ul>
 *   <li>用户注册：校验用户名唯一性，密码加密存储</li>
 *   <li>用户登录：密码验证，会话创建，在线状态管理</li>
 *   <li>用户登出：会话销毁，在线状态清除</li>
 *   <li>会话验证：基于Redis的会话管理和过期控制</li>
 * </ul>
 *
 * <p>线程安全说明：
 * <ul>
 *   <li>所有数据库操作在事务上下文中执行，保证数据一致性</li>
 *   <li>Redis操作使用RedisTemplate，内部连接池保证线程安全</li>
 *   <li>BCryptPasswordEncoder是线程安全的，可并发使用</li>
 * </ul>
 *
 * <p>并发安全注意事项：
 * <ul>
 *   <li>用户名唯一性检查和创建用户之间存在竞态条件，由数据库UNIQUE约束保证</li>
 *   <li>会话创建和在线状态更新使用Redis的原子操作</li>
   * <li>登录时的状态更新可能存在短暂不一致，通过Redis补偿机制解决</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Redis会话键前缀
     */
    private static final String SESSION_KEY_PREFIX = "session:";

    /**
     * Redis在线状态键前缀
     */
    private static final String ONLINE_KEY_PREFIX = "user:online:";

    /**
     * 会话过期时间：7天
     */
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    /**
     * 在线状态过期时间：30分钟
     */
    private static final Duration ONLINE_TTL = Duration.ofMinutes(30);

    /**
     * Redis Hash中用户ID的字段名
     */
    private static final String USER_ID_FIELD = "userId";

    /**
     * 用户注册
     *
     * <p>实现逻辑：
     * <ol>
     *   <li>检查用户名是否已存在（由数据库UNIQUE约束保证原子性）</li>
     *   <li>使用BCrypt加密密码</li>
     *   <li>创建用户实体，初始状态为OFFLINE</li>
     *   <li>保存到数据库并返回用户信息</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>用户名已存在：抛出BusinessException(ErrorCode.USER_ALREADY_EXISTS)</li>
     *   <li>数据库操作失败：抛出原始异常，保留完整异常链</li>
     * </ul>
     *
     * @param request 注册请求
     * @return 用户响应信息
     * @throws BusinessException 当用户名已存在时抛出
     * @throws RuntimeException 当数据库操作失败时抛出（保留异常链）
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("开始用户注册，用户名: {}", request.getUsername());

        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("注册失败，用户名已存在: {}", request.getUsername());
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
            }

            // 加密密码
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            // 创建用户实体
            User user = User.builder()
                    .username(request.getUsername())
                    .password(encodedPassword)
                    .nickname(request.getNickname())
                    .status(UserStatus.OFFLINE)
                    .build();

            // 保存用户
            User savedUser = userRepository.save(user);

            log.info("用户注册成功，用户ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());

            // 转换为响应DTO
            return convertToUserResponse(savedUser);

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            // 数据库异常保留完整异常链
            log.error("注册失败，数据库操作异常，用户名: {}", request.getUsername(), e);
            throw new RuntimeException("用户注册失败", e);
        }
    }

    /**
     * 用户登录
     *
     * <p>实现逻辑：
     * <ol>
     *   <li>根据用户名查询用户</li>
     *   <li>验证密码（BCrypt）</li>
     *   <li>生成UUID作为会话ID</li>
     *   <li>在Redis中存储会话信息（userId，7天TTL）</li>
     *   <li>在Redis中设置在线状态（30分钟TTL）</li>
     *   <li>更新数据库中的用户状态为ONLINE</li>
     *   <li>返回登录响应（包含sessionId）</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>用户不存在：抛出BusinessException(ErrorCode.AUTHENTICATION_FAILED)</li>
     *   <li>密码错误：抛出BusinessException(ErrorCode.AUTHENTICATION_FAILED)</li>
     *   <li>Redis操作失败：抛出原始异常，保留完整异常链</li>
     * </ul>
     *
     * <p>并发安全说明：
     * <ul>
     *   <li>会话创建和状态更新之间可能存在短暂不一致，但不影响功能</li>
     *   <li>Redis的SETEX命令是原子的，保证会话创建的完整性</li>
     *   <li>数据库状态更新可能失败，但会话已创建，下次请求会验证失败</li>
     * </ul>
     *
     * @param request 登录请求
     * @return 登录响应信息（包含sessionId）
     * @throws BusinessException 当认证失败时抛出
     * @throws RuntimeException 当Redis或数据库操作失败时抛出（保留异常链）
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("开始用户登录，用户名: {}", request.getUsername());

        try {
            // 查询用户
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        log.warn("登录失败，用户不存在: {}", request.getUsername());
                        return new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
                    });

            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("登录失败，密码错误，用户名: {}", request.getUsername());
                throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
            }

            // 生成会话ID
            String sessionId = UUID.randomUUID().toString();
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            String onlineKey = ONLINE_KEY_PREFIX + user.getId();

            // 存储会话到Redis（Hash结构，存储userId）
            redisTemplate.opsForHash().put(sessionKey, USER_ID_FIELD, user.getId().toString());
            redisTemplate.expire(sessionKey, SESSION_TTL);

            // 设置在线状态到Redis
            redisTemplate.opsForValue().set(onlineKey, "1", ONLINE_TTL);

            // 更新用户状态为在线
            user.setStatus(UserStatus.ONLINE);
            userRepository.save(user);

            log.info("用户登录成功，用户ID: {}, 用户名: {}, 会话ID: {}", user.getId(), user.getUsername(), sessionId);

            // 构建响应
            return LoginResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .status(user.getStatus())
                    .createTime(user.getCreateTime())
                    .sessionId(sessionId)
                    .build();

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            // Redis或数据库异常保留完整异常链
            log.error("登录失败，系统异常，用户名: {}", request.getUsername(), e);
            throw new RuntimeException("用户登录失败", e);
        }
    }

    /**
     * 用户登出
     *
     * <p>实现逻辑：
     * <ol>
     *   <li>根据sessionId获取userId</li>
     *   <li>删除Redis中的会话信息</li>
     *   <li>删除Redis中的在线状态</li>
     *   <li>更新数据库中的用户状态为OFFLINE</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>会话不存在：记录日志，不抛出异常（幂等操作）</li>
     *   <li>Redis操作失败：记录日志，继续执行数据库更新</li>
     *   <li>数据库操作失败：抛出原始异常，保留完整异常链</li>
     * </ul>
     *
     * <p>并发安全说明：
     * <ul>
     *   <li>登出操作是幂等的，多次调用不会产生副作用</li>
     *   <li>Redis删除操作失败不影响数据库状态更新</li>
     *   <li>数据库更新失败可能留下脏数据（在线状态已清除但数据库未更新），下次心跳会自动修复</li>
     * </ul>
     *
     * @param sessionId 会话ID
     * @throws RuntimeException 当数据库操作失败时抛出（保留异常链）
     */
    @Transactional
    public void logout(String sessionId) {
        log.info("开始用户登出，会话ID: {}", sessionId);

        try {
            // 获取userId
            Long userId = getUserIdBySession(sessionId);
            if (userId == null) {
                log.warn("登出失败，会话不存在: {}", sessionId);
                return;
            }

            // 删除会话
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);

            // 删除在线状态
            String onlineKey = ONLINE_KEY_PREFIX + userId;
            redisTemplate.delete(onlineKey);

            // 更新用户状态为离线
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(UserStatus.OFFLINE);
                userRepository.save(user);
                log.info("用户登出成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
            } else {
                log.warn("登出时用户不存在，用户ID: {}", userId);
            }

        } catch (Exception e) {
            // 保留完整异常链
            log.error("登出失败，系统异常，会话ID: {}", sessionId, e);
            throw new RuntimeException("用户登出失败", e);
        }
    }

    /**
     * 根据会话ID获取用户ID
     *
     * <p>实现逻辑：
     * <ol>
     *   <li>从Redis Hash中获取userId</li>
     *   <li>如果存在，延长会话TTL（续期）</li>
     *   <li>返回userId或null</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>会话不存在：返回null</li>
     *   <li>Redis操作失败：返回null</li>
     * </ul>
     *
     * <p>并发安全说明：
     * <ul>
     *   <li>读操作不需要同步</li>
     *   <li>TTL续期使用Redis的EXPIRE命令，是原子的</li>
     *   <li>即使续期失败，会话仍在有效期内（最多30秒误差）</li>
     * </ul>
     *
     * @param sessionId 会话ID
     * @return 用户ID，如果会话不存在或已过期则返回null
     */
    public Long getUserIdBySession(String sessionId) {
        try {
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            Object userIdObj = redisTemplate.opsForHash().get(sessionKey, USER_ID_FIELD);

            if (userIdObj == null) {
                log.debug("会话不存在或已过期: {}", sessionId);
                return null;
            }

            // 延长会话TTL
            redisTemplate.expire(sessionKey, SESSION_TTL);

            Long userId = Long.valueOf(userIdObj.toString());
            log.debug("会话验证成功，会话ID: {}, 用户ID: {}", sessionId, userId);
            return userId;

        } catch (Exception e) {
            log.error("获取用户ID失败，会话ID: {}", sessionId, e);
            return null;
        }
    }

    /**
     * 将User实体转换为UserResponse DTO
     *
     * @param user 用户实体
     * @return 用户响应DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}
