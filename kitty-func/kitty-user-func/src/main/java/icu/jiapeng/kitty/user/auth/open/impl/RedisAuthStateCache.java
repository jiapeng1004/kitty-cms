/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.auth.open.impl;

import jakarta.annotation.Resource;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 OAuth state 缓存，替代 JustAuth 默认内存实现。
 * 用于授权跳转时写入 state，回调时校验并取回，防止 CSRF；多实例/重启后 state 仍有效。
 */
@Component
public class RedisAuthStateCache implements AuthStateCache {

    private static final String KEY_PREFIX = "open:auth:state:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void cache(String key, String value) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + key, value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cache(String key, String value, long timeout) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + key, value, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(KEY_PREFIX + key);
    }

    @Override
    public boolean containsKey(String key) {
        Boolean has = stringRedisTemplate.hasKey(KEY_PREFIX + key);
        return Boolean.TRUE.equals(has);
    }
}
