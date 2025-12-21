package icu.jiapeng.kitty.material.infrastructure.cache;


import icu.jiapeng.kitty.material.domain.cache.CommonCacheOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Service
@ConditionalOnProperty(prefix = "infra", name = "cache", havingValue = "redis")
@RequiredArgsConstructor
public class StringRedisTemplateCacheOperator implements CommonCacheOperator {

    private final StringRedisTemplate stringRedisTemplate;

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
}
