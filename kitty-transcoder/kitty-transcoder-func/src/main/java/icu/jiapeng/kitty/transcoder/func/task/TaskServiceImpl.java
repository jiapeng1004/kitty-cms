package icu.jiapeng.kitty.transcoder.func.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.transcoder.api.CreateTaskRequest;
import icu.jiapeng.kitty.transcoder.api.ListTasksRequest;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import icu.jiapeng.kitty.transcoder.api.NotificationConfig;
import icu.jiapeng.kitty.transcoder.api.StepProgressItem;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeTask;
import icu.jiapeng.kitty.transcoder.func.engine.TranscodeEngine;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeTaskMapper;
import icu.jiapeng.kitty.transcoder.func.mapping.TaskVoMapper;
import icu.jiapeng.kitty.transcoder.api.TranscodeProgressNotifyVO;
import icu.jiapeng.kitty.transcoder.func.notification.NotificationDispatcher;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
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
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private ProgressBroadcaster progressBroadcaster;
    @Autowired
    private NotificationDispatcher notificationDispatcher;
    @Autowired
    private TaskCancellationRegistry cancellationRegistry;

    @Override
    public String createTask(CreateTaskRequest request, String createdByAk) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String inputPath = request.getInputPath() != null ? request.getInputPath() : request.getInputFile();
        if (inputPath == null || inputPath.isBlank()) {
            throw new IllegalArgumentException("输入不能为空");
        }
        String inputType = request.getInputType() != null ? request.getInputType() : "DISK";
        Integer priority = request.getPriority() != null ? request.getPriority() : 5;

        TranscodeTask entity = new TranscodeTask();
        entity.setId(taskId);
        entity.setTaskType("SCHEDULED_TRANSCODE");
        entity.setInputType(inputType);
        entity.setInputPath(inputPath);
        entity.setStrategyId(request.getStrategyId());
        entity.setWatermarkUrl(request.getWatermarkUrl());
        entity.setWatermarkPosition(request.getWatermarkPosition());
        if (request.getNotifications() != null && !request.getNotifications().isEmpty()) {
            entity.setNotificationConfig(JSON.toJSONString(request.getNotifications()));
        }
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
        if (entity == null) return null;
        TaskVO vo = taskVoMapper.toVO(entity);
        ensureNotifications(vo, entity);
        return vo;
    }

    /** 确保 notifications 从 notification_config 解析填充 */
    private void ensureNotifications(TaskVO vo, TranscodeTask entity) {
        if (vo == null || entity == null) return;
        if (vo.getNotifications() != null && !vo.getNotifications().isEmpty()) return;
        String raw = entity.getNotificationConfig();
        if (raw == null || raw.isBlank()) return;
        try {
            List<NotificationConfig> list = JSON.parseArray(raw, NotificationConfig.class);
            if (list != null && !list.isEmpty()) {
                vo.setNotifications(list);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<TaskVO> listTasks(ListTasksRequest req) {
        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int size = req.getSize() != null && req.getSize() > 0 ? req.getSize() : 20;
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TranscodeTask> p =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);

        String sortBy = req.getSortBy() != null && !req.getSortBy().isBlank() ? req.getSortBy() : "createdAt";
        String sortOrder = "asc".equalsIgnoreCase(req.getSortOrder()) ? "asc" : "desc";
        String sortCol = "completedAt".equalsIgnoreCase(sortBy) ? TranscodeTask.Fields.completedAt : TranscodeTask.Fields.createdAt;
        p.setOrders(java.util.Collections.singletonList(
                "asc".equals(sortOrder)
                        ? com.baomidou.mybatisplus.core.metadata.OrderItem.asc(StrUtil.toUnderlineCase(sortCol))
                        : com.baomidou.mybatisplus.core.metadata.OrderItem.desc(StrUtil.toUnderlineCase(sortCol))));

        LambdaQueryWrapper<TranscodeTask> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getTaskId())) {
            q.like(TranscodeTask::getId, req.getTaskId().trim());
        }
        if (StrUtil.isNotBlank(req.getFilename())) {
            q.like(TranscodeTask::getInputPath, req.getFilename().trim());
        }
        if (req.getTimeFrom() != null && req.getTimeFrom() > 0) {
            q.ge(TranscodeTask::getCreatedAt, java.time.Instant.ofEpochMilli(req.getTimeFrom()).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        if (req.getTimeTo() != null && req.getTimeTo() > 0) {
            q.le(TranscodeTask::getCreatedAt, java.time.Instant.ofEpochMilli(req.getTimeTo()).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        if (StrUtil.isNotBlank(req.getStrategyId())) {
            String sid = req.getStrategyId().trim();
            if ("__empty__".equals(sid)) {
                q.and(w -> w.isNull(TranscodeTask::getStrategyId).or().eq(TranscodeTask::getStrategyId, ""));
            } else {
                q.eq(TranscodeTask::getStrategyId, sid);
            }
        }
        if (StrUtil.isNotBlank(req.getStatus())) {
            q.eq(TranscodeTask::getStatus, req.getStatus().trim());
        }
        if (StrUtil.isNotBlank(req.getTaskType())) {
            q.eq(TranscodeTask::getTaskType, req.getTaskType().trim());
        }
        taskMapper.selectPage(p, q);
        List<TaskVO> list = p.getRecords().stream().map(taskVoMapper::toVO).toList();
        for (int i = 0; i < list.size(); i++) {
            ensureNotifications(list.get(i), p.getRecords().get(i));
        }
        return list;
    }

    @Override
    public boolean cancelTask(String taskId) {
        cancellationRegistry.markCancelled(taskId);
        TranscodeTask entity = taskMapper.selectById(taskId);
        LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
        u.eq(TranscodeTask::getId, taskId).set(TranscodeTask::getStatus, "CANCELLED");
        boolean ok = taskMapper.update(null, u) > 0;
        if (ok && entity != null) {
            entity.setStatus("CANCELLED");
            fireProgressNotification(taskId, entity, entity.getProgress() != null ? entity.getProgress() : 0);
        }
        return ok;
    }

    @Override
    public boolean deleteTask(String taskId) {
        cancellationRegistry.markCancelled(taskId);
        return taskMapper.deleteById(taskId) > 0;
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
            String outputBaseDir = computeOutputBaseDir(taskId, entity.getStrategyId());
            if (outputBaseDir != null) entity.setOutputPath(outputBaseDir);
            taskMapper.updateById(entity);
            fireProgressNotification(taskId, entity, 0);

            String localPath = entity.getInputPath();
            if ("HTTP".equalsIgnoreCase(entity.getInputType())) {
                localPath = resolveHttpInput(localPath, taskId, entity.getStrategyId());
            }
            String outputPath = transcodeEngine.transcode(taskId, localPath, entity.getStrategyId(),
                    entity.getWatermarkUrl(), entity.getWatermarkPosition(),
                    this::updateTaskStatus);
            String outputHttpUrl = buildOutputHttpUrl(outputPath);

            entity.setStatus("COMPLETED");
            entity.setProgress(100);
            entity.setOutputPath(outputPath);
            entity.setOutputHttpUrl(outputHttpUrl);
            entity.setCompletedAt(LocalDateTime.now());
            entity.setErrorMessage(null);
            taskMapper.updateById(entity);
            fireProgressNotification(taskId, entity, 100);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            String msg = cause != null ? cause.getMessage() : e.getMessage();
            boolean cancelled = msg != null && msg.contains("任务已取消");
            if (cancelled) {
                LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
                u.eq(TranscodeTask::getId, taskId).set(TranscodeTask::getStatus, "CANCELLED");
                taskMapper.update(null, u);
                TranscodeTask entity = taskMapper.selectById(taskId);
                if (entity != null) fireProgressNotification(taskId, entity, entity.getProgress() != null ? entity.getProgress() : 0);
            } else {
                if (msg != null && msg.startsWith("STEP_FAILED:")) {
                    int idx = msg.indexOf(':', 11);
                    msg = idx > 0 ? msg.substring(idx + 1) : msg;
                }
                updateTaskError(taskId, "转码失败：" + (msg != null ? msg : e.getClass().getSimpleName()));
                TranscodeTask entity = taskMapper.selectById(taskId);
                if (entity != null) {
                    entity.setStatus("FAILED");
                    taskMapper.updateById(entity);
                    fireProgressNotification(taskId, entity, entity.getProgress() != null ? entity.getProgress() : 0);
                }
            }
        } finally {
            cancellationRegistry.clear(taskId);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 计算输出基目录（transcoder/yyyy/MM/dd/），任务开始处理时即设置，便于执行中即可预览已完成步骤的输出。
     */
    private String computeOutputBaseDir(String taskId, String strategyId) {
        String workDir = transcodeConfig != null ? transcodeConfig.getWorkDir() : null;
        if (workDir == null || workDir.isBlank()) return null;
        if (strategyId != null && !strategyId.isBlank()) {
            var strategy = strategyService.getStrategy(strategyId);
            if (strategy != null && strategy.getWorkDir() != null && !strategy.getWorkDir().isBlank()) {
                workDir = strategy.getWorkDir();
            }
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String[] parts = datePath.split("/");
        if (parts.length < 3) return null;
        return Paths.get(workDir, "transcoder", parts[0], parts[1], parts[2]).normalize().toAbsolutePath().toString();
    }

    private String resolveHttpInput(String url, String taskId, String strategyId) {
        String workDir = transcodeConfig.getWorkDir();
        if (strategyId != null && !strategyId.isBlank()) {
            var strategy = strategyService.getStrategy(strategyId);
            if (strategy != null && strategy.getWorkDir() != null && !strategy.getWorkDir().isBlank()) {
                workDir = strategy.getWorkDir();
            }
        }
        return icu.jiapeng.kitty.transcoder.func.file.HttpFileHandler.downloadToTemp(url, taskId, workDir);
    }

    /**
     * 触发进度通知（SSE 广播 + HTTP 回调），异步执行，永不抛出异常。
     * 保证通知失败不影响转码任务执行。
     */
    private void fireProgressNotification(String taskId, TranscodeTask entity, int progress) {
        Thread.startVirtualThread(() -> {
            try {
                progressBroadcaster.broadcast(getProgress(taskId));
                sendProgressNotification(entity, progress);
            } catch (Throwable t) {
                // 静默忽略，通知逻辑不得影响任务执行
            }
        });
    }

    /** 任务进度/完成/失败时发送回调（HTTP 或 gRPC），统一使用 TranscodeProgressNotifyVO */
    private void sendProgressNotification(TranscodeTask entity, int progress) {
        if (entity == null || entity.getNotificationConfig() == null || entity.getNotificationConfig().isBlank()) return;
        try {
            List<NotificationConfig> configs = JSON.parseArray(entity.getNotificationConfig(), NotificationConfig.class);
            if (configs == null || configs.isEmpty()) return;
            TranscodeProgressNotifyVO vo = new TranscodeProgressNotifyVO();
            vo.setTaskId(entity.getId());
            vo.setStatus(entity.getStatus());
            vo.setProgress(progress);
            vo.setOutputPath(entity.getOutputPath());
            vo.setOutputHttpUrl(entity.getOutputHttpUrl());
            vo.setErrorMessage(entity.getErrorMessage());
            for (NotificationConfig nc : configs) {
                if (nc.getTarget() == null || nc.getTarget().isBlank()) continue;
                notificationDispatcher.dispatch(nc, vo);
            }
        } catch (Exception ignored) {
        }
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
        updateTaskStatus(taskId, status, progress, null);
    }

    @Override
    public void updateTaskStatus(String taskId, String status, int progress, List<StepProgressItem> stepProgressList) {
        // 与 SSE 一致：每次进度更新也触发 HTTP/gRPC 回调，实现持续通知
        TranscodeTask entity = taskMapper.selectById(taskId);
        if (entity != null) {
            Thread.startVirtualThread(() -> {
                try {
                    sendProgressNotification(entity, progress);
                } catch (Throwable t) {
                    // 静默忽略
                }
            });
        }
        LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
        u.eq(TranscodeTask::getId, taskId)
                .set(TranscodeTask::getStatus, status)
                .set(TranscodeTask::getProgress, progress);
        if (stepProgressList != null) {
            u.set(TranscodeTask::getProgressDetail, JSON.toJSONString(stepProgressList));
        }
        taskMapper.update(null, u);
        ProgressVO vo = getProgress(taskId);
        progressBroadcaster.broadcast(vo);
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
        if (entity != null && entity.getProgressDetail() != null && !entity.getProgressDetail().isBlank()) {
            try {
                List<StepProgressItem> list = JSON.parseArray(entity.getProgressDetail(), StepProgressItem.class);
                enrichStepDepends(list, entity.getStrategyId());
                vo.setStepProgressList(list);
                vo.setTotalSteps(list != null ? list.size() : null);
                if (list != null && !list.isEmpty()) {
                    String current = list.stream().filter(s -> "processing".equals(s.getStatus())).findFirst()
                            .map(StepProgressItem::getName).orElse(null);
                    vo.setCurrentStep(current);
                }
            } catch (Exception ignored) {
            }
        }
        return vo;
    }

    /** 从策略补充步骤依赖信息，确保前端能正确展示依赖关系 */
    private void enrichStepDepends(List<StepProgressItem> list, String strategyId) {
        if (list == null || list.isEmpty() || strategyId == null || strategyId.isBlank()) return;
        try {
            StrategyVO strategy = strategyService.getStrategy(strategyId);
            if (strategy == null || strategy.getSteps() == null) return;
            Map<Integer, String> dependsByStepId = new HashMap<>();
            for (StrategyStepVO step : strategy.getSteps()) {
                int sid = step.getStepId() != null ? step.getStepId() : 0;
                if (sid > 0) {
                    String d = step.getDepends();
                    dependsByStepId.put(sid, (d != null && !d.isBlank()) ? d.trim() : (sid == 1 ? "" : String.valueOf(sid - 1)));
                }
            }
            for (StepProgressItem item : list) {
                String d = dependsByStepId.get(item.getStepId());
                if (d != null) item.setDepends(d);
            }
        } catch (Exception ignored) {
        }
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

    @Override
    public SseEmitter getProgressStream() {
        return progressBroadcaster.subscribe();
    }

    @Override
    public void createMagicTaskRecord(String taskId, String taskType, String inputType, String inputPath) {
        TranscodeTask entity = new TranscodeTask();
        entity.setId(taskId);
        entity.setTaskType(taskType);
        entity.setInputType(inputType != null ? inputType : "DISK");
        entity.setInputPath(inputPath);
        entity.setStrategyId(null);
        entity.setStatus("PROCESSING");
        entity.setProgress(0);
        entity.setCreatedAt(LocalDateTime.now());
        taskMapper.insert(entity);
    }

    @Override
    public void completeMagicTask(String taskId, String outputPath, String outputHttpUrl) {
        LambdaUpdateWrapper<TranscodeTask> u = new LambdaUpdateWrapper<>();
        u.eq(TranscodeTask::getId, taskId)
                .set(TranscodeTask::getStatus, "COMPLETED")
                .set(TranscodeTask::getProgress, 100)
                .set(TranscodeTask::getOutputPath, outputPath)
                .set(TranscodeTask::getOutputHttpUrl, outputHttpUrl)
                .set(TranscodeTask::getCompletedAt, LocalDateTime.now())
                .set(TranscodeTask::getErrorMessage, null);
        taskMapper.update(null, u);
        TranscodeTask entity = taskMapper.selectById(taskId);
        if (entity != null) fireProgressNotification(taskId, entity, 100);
    }

}
