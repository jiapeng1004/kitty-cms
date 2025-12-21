package icu.jiapeng.kitty.material.domain.cache;


import java.util.concurrent.TimeUnit;

/**
 * 缓存操作器
 *
 * @author jiapeng
 * @since 2026/1/11
 */
public interface CommonCacheOperator {

    String get(String key);

    void set(String key, String value);

    void set(String key, String value, long timeout, TimeUnit timeUnit);
}
