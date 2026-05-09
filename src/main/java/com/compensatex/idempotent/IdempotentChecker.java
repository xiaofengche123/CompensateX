package com.compensatex.idempotent;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * Redis 优先的幂等校验器。
 * <p>
 * 通过 Redis 的 setIfAbsent 实现分布式幂等；当 Redis 不可用时降级到本地内存，保证组件可用性。
 */
public class IdempotentChecker {

    private static final Logger log = LoggerFactory.getLogger(IdempotentChecker.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "compensatex:idempotent:";

    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, Long> seenKeys = new ConcurrentHashMap<>();

    /**
     * 创建幂等校验器。
     *
     * @param redisTemplate Redis 模板，可为空
     */
    public IdempotentChecker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查并登记幂等键。
     *
     * @param key 幂等键
     * @return true 表示首次出现，false 表示重复
     */
    public boolean checkAndMark(String key) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        if (redisTemplate != null) {
            try {
                Boolean success = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + key, "1", DEFAULT_TTL);
                return Boolean.TRUE.equals(success);
            } catch (Exception ex) {
                log.warn("Redis idempotent check failed, fallback to memory. key={}", key, ex);
            }
        }
        return seenKeys.putIfAbsent(key, System.currentTimeMillis()) == null;
    }
}
