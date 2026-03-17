package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.RedisKeys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskQueueProcessor implements CommandLineRunner {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TaskService taskService;

    @Override
    public void run(String @NonNull ... args) {
        Thread.ofVirtual().unstarted(this::processTasks).start();
    }

    /**
     * 处理任务队列
     */
    private void processTasks() {
        RBlockingQueue<String> taskQueue = redissonClient.getBlockingQueue(RedisKeys.TASK_QUEUE_KEY);
        while (true) {
            try {
                // 从队列中取出任务
                String taskId = taskQueue.take();
                Thread.ofVirtual().unstarted(() -> {
                    try {
                        taskService.processTask(taskId);
                    } catch (Exception e) {
                        log.error("处理任务失败: {}", taskId, e);
                    }
                }).start();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                log.error("处理任务队列异常", e);
                break;
            }
        }
    }
}