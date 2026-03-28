package icu.jiapeng.kitty.material.support.cache;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 通用字符串缓存：基于 Spring Data Redis {@link StringRedisTemplate}（本模块唯一实现）。
 * <p>由 {@link icu.jiapeng.kitty.material.config.MamCacheConfig} 注册。</p>
 */
@RequiredArgsConstructor
public class StringRedisTemplateCacheOperator {

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

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public long increment(String key) {
        Long value = stringRedisTemplate.opsForValue().increment(key);
        return value == null ? 0L : value;
    }
}
