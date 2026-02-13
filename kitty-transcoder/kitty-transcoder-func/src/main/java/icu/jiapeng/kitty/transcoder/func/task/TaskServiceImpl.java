package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.api.CreateTaskRequest;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import icu.jiapeng.kitty.transcoder.func.engine.TranscodeEngine;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Service
public class TaskServiceImpl implements TaskService {

    private static final String TASK_QUEUE_KEY = "transcode:task:queue";
    private static final String TASK_MAP_KEY = "transcode:task:map";
    private static final String TASK_STATUS_KEY = "transcode:task:status:";

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private TranscodeEngine transcodeEngine;

    @Override
    public String createTask(CreateTaskRequest request) {
        // 生成任务ID
        String taskId = "task_" + UUID.randomUUID().toString().replace("-", "");

        // 创建任务对象
        TaskVO taskVO = new TaskVO();
        taskVO.setId(taskId);
        taskVO.setInputFile(request.getInputFile());
        taskVO.setStrategyId(request.getStrategyId());
        taskVO.setStatus("PENDING");
        taskVO.setProgress(0);
        taskVO.setCreatedAt(System.currentTimeMillis());

        // 存储任务信息到 Redis
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        taskMap.put(taskId, taskVO);

        // 将任务加入队列
        RBlockingQueue<String> taskQueue = redissonClient.getBlockingQueue(TASK_QUEUE_KEY);
        boolean offer = taskQueue.offer(taskId);

        return taskId;
    }

    @Override
    public TaskVO getTask(String taskId) {
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        return taskMap.get(taskId);
    }

    @Override
    public boolean cancelTask(String taskId) {
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        TaskVO taskVO = taskMap.get(taskId);
        if (taskVO != null) {
            taskVO.setStatus("CANCELLED");
            taskMap.put(taskId, taskVO);
            return true;
        }
        return false;
    }

    @Override
    public void processTask(String taskId) {
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        TaskVO taskVO = taskMap.get(taskId);
        if (taskVO != null && !"CANCELLED".equals(taskVO.getStatus())) {
            try {
                // 更新任务状态为处理中
                updateTaskStatus(taskId, "PROCESSING", 0);
                taskVO.setStartedAt(System.currentTimeMillis());
                taskMap.put(taskId, taskVO);

                // 执行转码
                String outputFile = transcodeEngine.transcode(taskId, taskVO.getInputFile(), taskVO.getStrategyId(), this::updateTaskStatus);

                // 更新任务状态为完成
                taskVO.setStatus("COMPLETED");
                taskVO.setProgress(100);
                taskVO.setOutputFile(outputFile);
                taskVO.setCompletedAt(System.currentTimeMillis());
                taskMap.put(taskId, taskVO);
            } catch (Exception e) {
                // 更新任务状态为失败
                updateTaskError(taskId, "转码失败：" + e.getMessage());
                taskVO.setStatus("FAILED");
                taskMap.put(taskId, taskVO);
            }
        }
    }

    @Override
    public void updateTaskStatus(String taskId, String status, int progress) {
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        TaskVO taskVO = taskMap.get(taskId);
        if (taskVO != null) {
            taskVO.setStatus(status);
            taskVO.setProgress(progress);
            taskMap.put(taskId, taskVO);
        }
    }

    @Override
    public void updateTaskError(String taskId, String errorMessage) {
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        TaskVO taskVO = taskMap.get(taskId);
        if (taskVO != null) {
            taskVO.setErrorMessage(errorMessage);
            taskMap.put(taskId, taskVO);
        }
    }

    @Override
    public ProgressVO getProgress(String taskId) {
        RMap<String, TaskVO> taskMap = redissonClient.getMap(TASK_MAP_KEY);
        TaskVO taskVO = taskMap.get(taskId);
        ProgressVO progressVO = new ProgressVO();
        progressVO.setTaskId(taskId);
        progressVO.setProgress(taskVO != null ? taskVO.getProgress() : 0);
        progressVO.setStatus(taskVO != null ? taskVO.getStatus() : "PENDING");
        return progressVO;
    }

    @Override
    public SseEmitter getProgressSSE(String taskId) {
        SseEmitter emitter = new SseEmitter();

        // 使用虚拟线程处理阻塞操作
        Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    // 获取进度
                    ProgressVO progress = getProgress(taskId);

                    // 发送 SSE 事件
                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(System.currentTimeMillis()))
                            .name("progress")
                            .data(progress));

                    // 检查任务是否完成
                    if ("COMPLETED".equals(progress.getStatus()) ||
                            "FAILED".equals(progress.getStatus()) ||
                            "CANCELLED".equals(progress.getStatus())) {
                        emitter.complete();
                        break;
                    }
                    // 等待 1 秒
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}