package icu.jiapeng.kitty.transcoder.func.task;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务取消信号：cancelTask 时标记，转码循环中轮询检查，可中断卡住的任务。
 */
@Component
public class TaskCancellationRegistry {

    private final ConcurrentHashMap<String, Boolean> cancelled = new ConcurrentHashMap<>();

    public void markCancelled(String taskId) {
        if (taskId != null && !taskId.isBlank()) {
            cancelled.put(taskId, Boolean.TRUE);
        }
    }

    public boolean isCancelled(String taskId) {
        return taskId != null && Boolean.TRUE.equals(cancelled.get(taskId));
    }

    /** 任务结束时清理，避免内存泄漏 */
    public void clear(String taskId) {
        if (taskId != null) {
            cancelled.remove(taskId);
        }
    }
}
