package icu.jiapeng.kitty.plugin.s3.port;

import java.util.function.Supplier;

/**
 * 分布式锁端口抽象
 * 适配不同分布式锁实现
 */
public interface DistributedLockPort {

    /**
     * 尝试获取锁
     *
     * @param key           锁Key
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    boolean tryLock(String key, long expireSeconds);

    /**
     * 释放锁
     *
     * @param key 锁Key
     */
    void unlock(String key);

    /**
     * 执行锁保护的操作
     *
     * @param key           锁Key
     * @param expireSeconds 过期时间
     * @param action        要执行的操作
     * @param <T>           返回值类型
     * @return 操作结果
     */
    <T> T executeWithLock(String key, long expireSeconds, Supplier<T> action);

    /**
     * 执行锁保护的操作（无返回值）
     *
     * @param key           锁Key
     * @param expireSeconds 过期时间
     * @param action        要执行的操作
     */
    default void executeWithLock(String key, long expireSeconds, Runnable action) {
        executeWithLock(key, expireSeconds, () -> {
            action.run();
            return null;
        });
    }
}