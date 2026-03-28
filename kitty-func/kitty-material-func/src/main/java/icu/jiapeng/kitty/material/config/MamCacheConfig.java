package icu.jiapeng.kitty.material.config;

import icu.jiapeng.kitty.material.support.cache.StringRedisTemplateCacheOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 通用缓存：仅 {@link StringRedisTemplate}（Redis）实现。
 * <p>由 {@link MamFuncConfig} {@code @Import} 引入。</p>
 */
@Configuration
public class MamCacheConfig {

    @Bean
    public StringRedisTemplateCacheOperator stringRedisTemplateCacheOperator(StringRedisTemplate stringRedisTemplate) {
        return new StringRedisTemplateCacheOperator(stringRedisTemplate);
    }
}
