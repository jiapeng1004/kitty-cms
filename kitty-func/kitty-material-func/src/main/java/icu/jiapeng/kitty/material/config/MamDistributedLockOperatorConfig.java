package icu.jiapeng.kitty.material.config;

import icu.jiapeng.kitty.material.support.lock.RedissonDistributedLockOperator;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式锁：仅 Redisson（无 JVM / RedisTemplate 回退）。
 */
@Configuration
public class MamDistributedLockOperatorConfig {

    @Bean
    public RedissonDistributedLockOperator redissonDistributedLockOperator(RedissonClient redissonClient) {
        return new RedissonDistributedLockOperator(redissonClient);
    }
}
