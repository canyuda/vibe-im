package com.vibe.im.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应DTO
 *
 * <p>通用的分页响应数据传输对象，用于包装分页查询结果。
 * 包含分页元数据和内容列表，支持泛型以适配不同类型的分页数据。
 *
 * <p>设计说明：
 * <ul>
 *   <li>使用泛型T支持任意类型的分页数据</li>
 *   <li>提供静态工厂方法从Spring Data的Page对象构建</li>
 *   <li>采用不可变设计，所有字段通过构造器初始化</li>
 * </ul>
 *
 * @param <T> 分页数据元素的类型
 * @author AI Assistant
 * @since 1.0.0
 */
public record PageResponse<T>(
    /**
     * 分页内容列表
     */
    List<T> content,

    /**
     * 当前页码（从0开始）
     */
    int currentPage,

    /**
     * 总页数
     */
    int totalPages,

    /**
     * 总记录数
     */
    long totalElements,

    /**
     * 每页记录数
     */
    int pageSize
) {

    /**
     * 从Spring Data的Page对象构建分页响应
     *
     * @param page Spring Data分页对象
     * @param <T> 分页数据元素的类型
     * @return 分页响应DTO
     * @throws IllegalArgumentException 如果page为null
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        if (page == null) {
            throw new IllegalArgumentException("Page对象不能为null");
        }
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize()
        );
    }

    /**
     * 判断是否为空页面
     *
     * @return 如果没有数据返回true，否则返回false
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    /**
     * 判断是否有下一页
     *
     * @return 如果当前页小于总页数返回true，否则返回false
     */
    public boolean hasNext() {
        return currentPage + 1 < totalPages;
    }

    /**
     * 判断是否有上一页
     *
     * @return 如果当前页大于0返回true，否则返回false
     */
    public boolean hasPrevious() {
        return currentPage > 0;
    }
}
