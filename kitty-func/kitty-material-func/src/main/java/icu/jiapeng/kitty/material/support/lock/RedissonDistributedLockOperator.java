package icu.jiapeng.kitty.material.support.lock;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁：基于 Redisson {@link RLock}（本模块唯一实现）。
 */
@RequiredArgsConstructor
public class RedissonDistributedLockOperator {

    private final RedissonClient redissonClient;

    public void executeWithLock(String lockKey, long waitMillis, long leaseSeconds, Runnable action) {
        if (lockKey == null || lockKey.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (leaseSeconds <= 0 || waitMillis < 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean ok = lock.tryLock(waitMillis, leaseSeconds, TimeUnit.SECONDS);
            if (!ok) {
                throw BizException.of(ResultStatus.NORMAL_ERROR);
            }
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BizException.of(ResultStatus.NORMAL_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
