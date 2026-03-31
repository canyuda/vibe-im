package com.vibe.im.repository;

import com.vibe.im.entity.User;
import com.vibe.im.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 提供用户相关的数据库操作方法
 *
 * <p>主要功能：
 * <ul>
 *   <li>根据用户名查询用户</li>
 *   <li>检查用户名是否存在</li>
 *   <li>根据用户状态查询用户列表</li>
 *   <li>批量更新用户状态</li>
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
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，如果不存在则返回Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户状态查询用户列表
     *
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 根据用户ID和状态查询用户
     *
     * @param id 用户ID
     * @param status 用户状态
     * @return 用户对象，如果不存在则返回Optional.empty()
     */
    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    /**
     * 批量更新用户状态为离线
     * 用于系统关闭或用户批量下线场景
     *
     * @param status 当前状态（通常是ONLINE）
     * @return 更新的记录数
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.status = 'OFFLINE' WHERE u.status = :status")
    int updateAllStatusToOffline(@org.springframework.data.repository.query.Param("status") UserStatus status);
}
