package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.RedisKeys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务取消信号（分布式）：本地 cache + Redis 发布订阅。
 * <ul>
 *   <li>本地 cache：与任务生命周期一致，执行节点在 processTask 开始时 registerRunning，结束时 clear。</li>
 *   <li>取消时：先写 Redis 键；若为本实例正在执行的任务则直接标记本地 cache，否则通过 Redis 发布订阅广播，执行节点监听到后更新本地 cache。</li>
 *   <li>任务进程/线程本身只检查本地 cache（不查 Redis），避免热路径的远程开销；分布式取消通过发布订阅推到正确执行节点并写入其本地 cache。</li>
 *   <li>非本实例执行的任务（如尚未开始或其它节点执行）才回退到查 Redis 键。</li>
 * </ul>
 */
@Component
@Slf4j
public class TaskCancellationRegistry implements InitializingBean, DisposableBean {

    private static final long CANCEL_TTL_HOURS = 24;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 本节点已收到取消通知的 taskId（含本节点发起或订阅收到的），与任务生命周期一致，clear 时移除
     */
    private final Set<String> cancelledLocally = ConcurrentHashMap.newKeySet();
    /**
     * 本节点当前正在执行的任务，用于取消时判断是否直接写本地 cache
     */
    private final Set<String> runningOnThisInstance = ConcurrentHashMap.newKeySet();

    private volatile RTopic topic;

    /**
     * 标记本实例开始执行该任务，与任务生命周期一致；clear 时会 unregister。
     * 删除/取消时通过 markCancelled 写本地 cache，执行中通过 isCancelled(taskId) 协作退出。
     */
    public void registerRunning(String taskId) {
        if (taskId != null && !taskId.isBlank()) {
            runningOnThisInstance.add(taskId);
        }
    }

    /**
     * 标记任务为已取消。写 Redis 键；若为本实例正在执行的任务则直接写本地 cache，否则发布到 Redis channel 供执行节点订阅更新本地 cache。
     */
    public void markCancelled(String taskId) {
        if (taskId == null || taskId.isBlank()) return;
        try {
            RBucket<String> bucket = redissonClient.getBucket(RedisKeys.CANCEL_KEY_PREFIX + taskId);
            bucket.set("1", Duration.ofHours(CANCEL_TTL_HOURS));
            if (runningOnThisInstance.contains(taskId)) {
                cancelledLocally.add(taskId);
            } else {
                RTopic t = redissonClient.getTopic(RedisKeys.CANCEL_CHANNEL);
                t.publish(taskId);
            }
        } catch (Exception e) {
            log.warn("markCancelled failed: taskId={}", taskId, e);
        }
    }

    /**
     * 是否已取消。任务线程热路径只查本地 cache；若为本实例正在执行的任务则不查 Redis，由发布订阅已将取消推到本节点并写入本地 cache。
     * 非本实例执行的任务才回退到查 Redis 键。
     */
    public boolean isCancelled(String taskId) {
        if (taskId == null || taskId.isBlank()) return false;
        if (cancelledLocally.contains(taskId)) return true;
        if (runningOnThisInstance.contains(taskId)) return false;
        try {
            return redissonClient.getBucket(RedisKeys.CANCEL_KEY_PREFIX + taskId).isExists();
        } catch (Exception e) {
            log.debug("isCancelled redis failed: taskId={}", taskId, e);
            return false;
        }
    }

    /**
     * 任务结束时清理：移除 Redis 键、本地取消标记、本实例运行标记。
     */
    public void clear(String taskId) {
        if (taskId == null || taskId.isBlank()) return;
        runningOnThisInstance.remove(taskId);
        cancelledLocally.remove(taskId);
        try {
            redissonClient.getBucket(RedisKeys.CANCEL_KEY_PREFIX + taskId).delete();
        } catch (Exception e) {
            log.debug("clear redis failed: taskId={}", taskId, e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        subscribe();
    }

    protected void subscribe() {
        try {
            topic = redissonClient.getTopic(RedisKeys.CANCEL_CHANNEL);
            topic.addListener(String.class, (_, taskId) -> {
                if (taskId != null && !taskId.isBlank()) {
                    cancelledLocally.add(taskId);
                    log.debug("Received cancel via pub/sub: taskId={}", taskId);
                }
            });
        } catch (Exception e) {
            log.warn("Subscribe to cancel channel failed", e);
        }
    }

    public void destroy() {
        if (topic != null) {
            topic.removeAllListeners();
        }
    }

}
