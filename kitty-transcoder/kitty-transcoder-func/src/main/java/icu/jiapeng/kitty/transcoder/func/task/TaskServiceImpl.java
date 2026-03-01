package icu.jiapeng.kitty.transcoder.func.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import icu.jiapeng.kitty.transcoder.api.CreateTaskRequest;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeTask;
import icu.jiapeng.kitty.transcoder.func.engine.TranscodeEngine;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeTaskMapper;
import icu.jiapeng.kitty.transcoder.func.mapping.TaskVoMapper;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Service
public class TaskServiceImpl implements TaskService {

    private static final String TASK_QUEUE_KEY = "transcode:task:queue";
    private static final String TASK_LOCK_PREFIX = "transcode:task:lock:";

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TranscodeTaskMapper taskMapper;
    @Autowired
    private TranscodeEngine transcodeEngine;
    @Autowired
    private TranscodeConfig transcodeConfig;
    @Autowired
    private TaskVoMapper taskVoMapper;

    @Override
    public String createTask(CreateTaskRequest request, String createdByAk) {
        String taskId = "task_" + UUID.randomUUID().toString().replace("-", "");
        String inputPath = request.getInputPath() != null ? request.getInputPath() : request.getInputFile();
        if (inputPath == null || inputPath.isBlank()) {
            throw new IllegalArgumentException("输入不能为空");
        }
        String inputType = request.getInputType() != null ? request.getInputType() : "DISK";
        Integer priority = request.getPriority() != null ? request.getPriority() : 5;

        TranscodeTask entity = new TranscodeTask();
        entity.setId(taskId);
        entity.setInputType(inputType);
        entity.setInputPath(inputPath);
        entity.setStrategyId(request.getStrategyId());
        entity.setStatus("PENDING");
        entity.setProgress(0);
        entity.setPriority(priority);
        entity.setRetryCount(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedByAk(createdByAk);
        taskMapper.insert(entity);

        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(TASK_QUEUE_KEY);
        queue.offer(taskId);
        return taskId;
    }

    @Override
    public TaskVO getTask(String taskId) {
        TranscodeTask entity = taskMapper.selectById(taskId);
        return entity == null ? null : taskVoMapper.toVO(entity);
    }

    @Override
    public List<TaskVO> listTasks(int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TranscodeTask> p =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        p.setOrders(java.util.Collections.singletonList(
                com.baomidou.mybatisplus.core.metadata.OrderItem.desc(StrUtil.toUnderlineCase(TranscodeTask.Fields.completedAt))));
        taskMapper.selectPage(p, null);
        return p.getRecords().stream().map(taskVoMapper::toVO).toList();
    }

    @Override
    public boolean cancelTask(String taskId) {
        LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
        u.eq(TranscodeTask::getId, taskId).set(TranscodeTask::getStatus, "CANCELLED");
        return taskMapper.update(null, u) > 0;
    }

    @Override
    public void processTask(String taskId) {
        RLock lock = redissonClient.getLock(TASK_LOCK_PREFIX + taskId);
        try {
            if (!lock.tryLock(2, 300, TimeUnit.SECONDS)) {
                return;
            }
            TranscodeTask entity = taskMapper.selectById(taskId);
            if (entity == null || "CANCELLED".equals(entity.getStatus())) {
                return;
            }
            entity.setStatus("PROCESSING");
            entity.setStartedAt(LocalDateTime.now());
            taskMapper.updateById(entity);

            String localPath = entity.getInputPath();
            if ("HTTP".equalsIgnoreCase(entity.getInputType())) {
                localPath = resolveHttpInput(localPath, taskId);
            }
            String outputPath = transcodeEngine.transcode(taskId, localPath, entity.getStrategyId(), this::updateTaskStatus);
            String outputHttpUrl = buildOutputHttpUrl(outputPath);

            entity.setStatus("COMPLETED");
            entity.setProgress(100);
            entity.setOutputPath(outputPath);
            entity.setOutputHttpUrl(outputHttpUrl);
            entity.setCompletedAt(LocalDateTime.now());
            entity.setErrorMessage(null);
            taskMapper.updateById(entity);
        } catch (Exception e) {
            updateTaskError(taskId, "转码失败：" + e.getMessage());
            TranscodeTask entity = taskMapper.selectById(taskId);
            if (entity != null) {
                entity.setStatus("FAILED");
                taskMapper.updateById(entity);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String resolveHttpInput(String url, String taskId) {
        return icu.jiapeng.kitty.transcoder.func.file.HttpFileHandler.downloadToTemp(url, taskId, transcodeConfig.getTempDir());
    }

    private String buildOutputHttpUrl(String outputPath) {
        String prefix = transcodeConfig.getOutput() != null && transcodeConfig.getOutput().getHttpPrefix() != null
                ? transcodeConfig.getOutput().getHttpPrefix() : "";
        if (prefix.isEmpty()) return null;
        if (outputPath == null) return null;
        if (outputPath.startsWith(prefix)) return outputPath;
        if (prefix.endsWith("/")) return prefix + outputPath;
        return prefix + "/" + outputPath;
    }

    @Override
    public void updateTaskStatus(String taskId, String status, int progress) {
        LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
        u.eq(TranscodeTask::getId, taskId)
                .set(TranscodeTask::getStatus, status)
                .set(TranscodeTask::getProgress, progress);
        taskMapper.update(null, u);
    }

    @Override
    public void updateTaskError(String taskId, String errorMessage) {
        LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
        u.eq(TranscodeTask::getId, taskId).set(TranscodeTask::getErrorMessage, errorMessage);
        taskMapper.update(null, u);
    }

    @Override
    public ProgressVO getProgress(String taskId) {
        TranscodeTask entity = taskMapper.selectById(taskId);
        ProgressVO vo = new ProgressVO();
        vo.setTaskId(taskId);
        vo.setProgress(entity != null ? entity.getProgress() : 0);
        vo.setStatus(entity != null ? entity.getStatus() : "PENDING");
        return vo;
    }

    @Override
    public SseEmitter getProgressSSE(String taskId) {
        SseEmitter emitter = new SseEmitter();
        Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    ProgressVO progress = getProgress(taskId);
                    emitter.send(SseEmitter.event().id(String.valueOf(System.currentTimeMillis())).name("progress").data(progress));
                    if ("COMPLETED".equals(progress.getStatus()) || "FAILED".equals(progress.getStatus()) || "CANCELLED".equals(progress.getStatus())) {
                        emitter.complete();
                        break;
                    }
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

}
