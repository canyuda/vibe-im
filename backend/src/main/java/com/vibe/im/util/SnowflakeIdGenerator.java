package com.vibe.im.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Snowflake分布式ID生成器
 * 基于Twitter Snowflake算法实现，生成64位长整型唯一ID
 *
 * <p>算法结构：
 * <ul>
 *   <li>1位符号位（始终为0）</li>
 *   <li>41位时间戳（毫秒级，相对于纪元时间）</li>
 *   <li>10位工作机器ID（支持1024个节点）</li>
 *   <li>12位序列号（每毫秒可生成4096个ID）</li>
 * </ul>
 *
 * <p>线程安全说明：
 * <ul>
 *   <li>使用synchronized方法保证同一时刻只有一个线程生成ID</li>
 *   <li>sequence变量使用AtomicLong保证原子性</li>
 *   <li>在时钟回拨场景下会抛出异常，防止生成重复ID</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 */
@Component
public class SnowflakeIdGenerator {

    /**
     * 纪元时间（2024-01-01 00:00:00 UTC）
     */
    private static final long EPOCH = 1704067200000L;

    /**
     * 工作机器ID所占的位数
     */
    private static final long WORKER_ID_BITS = 10L;

    /**
     * 序列号所占的位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 工作机器ID的最大值（1024）
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 序列号的最大值（4096）
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID左移位数（12位）
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 时间戳左移位数（22位）
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 工作机器ID（默认为1，可通过配置文件注入）
     */
    @Value("${snowflake.worker-id:1}")
    private long workerId;

    /**
     * 序列号（使用AtomicLong保证原子性）
     */
    private final AtomicLong sequence = new AtomicLong(0L);

    /**
     * 上一次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数，初始化工作机器ID
     */
    public SnowflakeIdGenerator() {
        // 默认构造函数，workerId通过@Value注入
    }

    /**
     * 生成下一个唯一ID（线程安全）
     *
     * @return 唯一的64位长整型ID字符串
     * @throws IllegalStateException 当时钟回拨时抛出异常
     */
    public synchronized String generateId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨检查
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp)
            );
        }

        // 同一毫秒内，序列号自增
        if (timestamp == lastTimestamp) {
            long currentSequence = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (currentSequence == 0) {
                // 序列号溢出，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
                sequence.set(0L);
            }
        } else {
            // 新的毫秒，重置序列号
            sequence.set(0L);
        }

        // 更新最后时间戳
        lastTimestamp = timestamp;

        // 生成ID
        long id = ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();

        return String.valueOf(id);
    }

    /**
     * 阻塞直到下一毫秒
     *
     * @param lastTimestamp 上一次时间戳
     * @return 当前时间戳（毫秒）
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 解析Snowflake ID，返回生成时间戳
     *
     * @param id Snowflake ID
     * @return 生成时间戳（毫秒）
     */
    public long parseTimestamp(long id) {
        return ((id >> TIMESTAMP_SHIFT) & ~(-1L << (64 - TIMESTAMP_SHIFT))) + EPOCH;
    }

    /**
     * 解析Snowflake ID，返回工作机器ID
     *
     * @param id Snowflake ID
     * @return 工作机器ID
     */
    public long parseWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 解析Snowflake ID，返回序列号
     *
     * @param id Snowflake ID
     * @return 序列号
     */
    public long parseSequence(long id) {
        return id & MAX_SEQUENCE;
    }
}
