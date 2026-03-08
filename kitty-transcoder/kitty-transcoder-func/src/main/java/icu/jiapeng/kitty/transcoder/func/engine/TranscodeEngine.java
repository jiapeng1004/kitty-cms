package icu.jiapeng.kitty.transcoder.func.engine;

import cn.hutool.core.util.NumberUtil;
import icu.jiapeng.kitty.transcoder.api.StepProgressItem;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.TaskStatus;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import icu.jiapeng.kitty.transcoder.func.task.TaskCancellationRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TranscodeEngine {

    @Resource
    private StrategyService strategyService;

    @Resource
    private TranscodeConfig transcodeConfig;

    @Resource
    private TaskCancellationRegistry taskCancellationRegistry;

    private final ExecutorService stepExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public interface ProgressCallback {
        void updateProgress(String taskId, String status, int progress, java.util.List<StepProgressItem> stepProgressList);
    }

    /**
     * 同步单目标转码（魔法接口）：无策略，直接按参数转码。
     */
    public String transcodeSingleTarget(String taskId, String inputFile,
                                        String targetFormat, String resolution, Integer bitrate, Integer frameRate,
                                        ProgressCallback progressCallback) {
        StrategyStepVO step = new StrategyStepVO();
        step.setStepId(1);
        step.setType(StepExecutorType.TRANSCODE.getCode());
        step.setTargetFormat(targetFormat != null && !targetFormat.isBlank() ? targetFormat : "mp4");
        step.setResolution(resolution != null && !resolution.isBlank() ? resolution : "1920x1080");
        step.setBitrate(bitrate != null ? bitrate : 5000);
        step.setFrameRate(frameRate != null ? frameRate : 30);
        step.setEncoder("h264");
        step.setDepends("");
        StrategyVO strategy = new StrategyVO();
        strategy.setSteps(Collections.singletonList(step));
        strategy.setWorkDir(transcodeConfig.getWorkDir());
        return runWithDependencies(taskId, inputFile, strategy, null, null, progressCallback);
    }

    public String transcode(String taskId, String inputFile, String strategyId,
                            String watermarkUrl, String watermarkPosition, ProgressCallback progressCallback) throws Exception {
        StrategyVO strategy = strategyService.getStrategy(strategyId);
        if (strategy == null || strategy.getSteps() == null || strategy.getSteps().isEmpty()) {
            StrategyStepVO defaultStep = new StrategyStepVO();
            defaultStep.setType(StepExecutorType.TRANSCODE.getCode());
            defaultStep.setTargetFormat("mp4");
            defaultStep.setResolution("1920x1080");
            defaultStep.setBitrate(5000);
            defaultStep.setFrameRate(30);
            defaultStep.setEncoder("h264");
            StepContextImpl ctx = new StepContextImpl((_, _) -> {
                throw new UnsupportedOperationException();
            });
            ctx.setTaskId(taskId);
            ctx.setCancellationChecker(() -> taskCancellationRegistry.isCancelled(taskId));
            ctx.setWatermarkUrl(watermarkUrl);
            ctx.setWatermarkPosition(watermarkPosition);
            ctx.setWorkDir(transcodeConfig.getWorkDir());
            StepExecutor exec = StepExecutor.Factory.resolveOrFail(StepExecutorType.TRANSCODE.getCode());
            return exec.execute(inputFile, defaultStep, "_transcoded", ctx);
        }
        return runWithDependencies(taskId, inputFile, strategy, watermarkUrl, watermarkPosition, progressCallback);
    }

    private String runWithDependencies(String taskId, String taskInputPath, StrategyVO strategy,
                                       String watermarkUrl, String watermarkPosition, ProgressCallback progressCallback) {
        List<StrategyStepVO> steps = strategy.getSteps();
        Map<Integer, StrategyStepVO> stepByStepId = steps.stream().collect(Collectors.toMap(StrategyStepVO::getStepId, Function.identity(), (o1, _)->o1));
        List<Integer> stepIds = stepByStepId.keySet().stream().toList();
        Map<Integer, Integer[]> depsMap = new HashMap<>();
        for (Map.Entry<Integer, StrategyStepVO> e : stepByStepId.entrySet()) {
            depsMap.put(e.getKey(), Arrays.stream(e.getValue().getDepends().split( ",")).filter(NumberUtil::isInteger).map(Integer::valueOf).toArray(Integer[]::new));
        }
        StepContextImpl.RunStrategyCallback runStrategyCallback = (strategyId, inputPath) -> {
            StrategyVO sub = strategyService.getStrategy(strategyId);
            if (sub == null) throw new IllegalArgumentException("策略不存在：" + strategyId);
            return runWithDependencies(taskId, inputPath, sub, watermarkUrl, watermarkPosition, progressCallback);
        };
        StepContextImpl ctx = new StepContextImpl(runStrategyCallback);
        ctx.setTaskId(taskId);
        ctx.setCancellationChecker(() -> taskCancellationRegistry.isCancelled(taskId));
        ctx.setWatermarkUrl(watermarkUrl);
        ctx.setWatermarkPosition(watermarkPosition);
        String workDir = (strategy.getWorkDir() != null && !strategy.getWorkDir().isBlank())
                ? strategy.getWorkDir() : transcodeConfig.getWorkDir();
        ctx.setWorkDir(workDir);

        List<List<Integer>> levels = buildLevels(stepIds, depsMap);
        Map<Integer, String> stepOutputs = new ConcurrentHashMap<>();
        int completed = 0;
        int n = stepIds.size();
        Map<Integer, AtomicInteger> currentLevelProgress = new ConcurrentHashMap<>();
        for (List<Integer> level : levels) {
            final int completedBeforeLevel = completed;
            currentLevelProgress.clear();
            for (int sid : level) currentLevelProgress.put(sid, new AtomicInteger(0));
            final List<Integer> levelSteps = level;
            BiConsumer<Integer, Integer> reporter = (stepIndex, percent) -> {
                AtomicInteger ai = currentLevelProgress.get(stepIndex);
                if (ai != null) ai.set(Math.min(100, Math.max(0, percent)));
                if (progressCallback == null) return;
                List<StepProgressItem> merged = buildStepProgressListWithLevelProgress(
                        stepByStepId, stepIds, completedBeforeLevel, levelSteps, currentLevelProgress);
                int sum = completedBeforeLevel * 100;
                for (int stepId : levelSteps) {
                    AtomicInteger p = currentLevelProgress.get(stepId);
                    sum += (p != null ? p.get() : 0);
                }
                int overall = n > 0 ? Math.min(100, sum / n) : 0;
                progressCallback.updateProgress(taskId, TaskStatus.PROCESSING, overall, merged);
            };
            ctx.setStepProgressReporter(reporter);
            List<StepProgressItem> stepList = buildStepProgressList(stepByStepId, stepIds, completed, level, false);
            if (progressCallback != null && !stepList.isEmpty()) {
                progressCallback.updateProgress(taskId, TaskStatus.PROCESSING, (completed * 100) / n, stepList);
            }
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Integer sid : level) {
                StrategyStepVO step = stepByStepId.get(sid);
                if (step == null) continue;
                Integer[] deps = depsMap.get(sid);
                String inputPath;
                if (step.getInputTemplate() != null && !step.getInputTemplate().isBlank()) {
                    inputPath = StepTemplateResolver.resolve(step.getInputTemplate(), taskId, taskInputPath, stepOutputs, sid, workDir);
                } else {
                    inputPath = deps.length == 0 ? taskInputPath : stepOutputs.get(maxOf(deps));
                }
                if (inputPath == null || inputPath.isBlank())
                    throw new IllegalStateException("步骤 " + sid + " 输入路径为空");
                inputPath = MediaStepOps.toLocalFilePath(inputPath, workDir);
                String resolvedOutputPath = null;
                if (step.getOutputTemplate() != null && !step.getOutputTemplate().isBlank()) {
                    resolvedOutputPath = StepTemplateResolver.resolve(step.getOutputTemplate(), taskId, taskInputPath, stepOutputs, sid, workDir);
                    resolvedOutputPath = MediaStepOps.toLocalFilePath(resolvedOutputPath, workDir);
                }
                int inputStepId = deps.length == 0 ? -1 : maxOf(deps);
                String stepSuffix = "_s" + sid;
                StepExecutor exec = StepExecutor.Factory.resolveOrFail(step.getType());
                final String inp = inputPath;
                final int stepIdForPut = sid;
                final String workDirFinal = workDir;
                final String resolvedForStep = resolvedOutputPath;
                final int inputStepIdFinal = inputStepId;
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    try {
                        ctx.setInputStepIndex(inputStepIdFinal);
                        ctx.setResolvedOutputPath(resolvedForStep);
                        ctx.setCurrentStepIndex(stepIdForPut);
                        String out = exec.execute(inp, step, stepSuffix, ctx);
                        out = MediaStepOps.toLocalFilePath(out, workDirFinal);
                        stepOutputs.put(stepIdForPut, out);
                    } catch (Exception e) {
                        log.error("步骤 {} 执行失败", sid, e);
                        throw new RuntimeException(TranscodeConstants.STEP_FAILED_PREFIX + sid + ":" + e.getMessage(), e);
                    } finally {
                        StepContextImpl.clearCurrentStepIndexForThread();
                    }
                }, stepExecutor);
                futures.add(f);
            }
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (CompletionException e) {
                int failedStepId = extractFailedStepId(e.getCause());
                if (progressCallback != null && failedStepId > 0) {
                    List<StepProgressItem> failedList = buildStepProgressListWithFailure(
                            stepByStepId, stepIds, completedBeforeLevel, levelSteps, failedStepId);
                    progressCallback.updateProgress(taskId, TaskStatus.FAILED,
                            Math.min(100, (completedBeforeLevel + levelSteps.size()) * 100 / n), failedList);
                }
                throw e;
            }
            completed += level.size();
            List<StepProgressItem> stepListDone = buildStepProgressList(stepByStepId, stepIds, completed, Collections.emptyList(), true);
            if (progressCallback != null && !stepListDone.isEmpty()) {
                progressCallback.updateProgress(taskId, "PROCESSING", (completed * 100) / n, stepListDone);
            }
        }

        int lastStep = findLastStepByTopology(stepIds, depsMap, stepByStepId);
        String lastOutput = stepOutputs.get(lastStep);
        if (lastOutput == null) throw new IllegalStateException("无最终输出");
        return lastOutput;
    }

    private static Integer maxOf(Integer[] a) {
        if (a.length == 0) return -1;
        Integer m = a[0];
        for (int i = 1; i < a.length; i++) if (a[i] > m) m = a[i];
        return m;
    }

    private static int extractFailedStepId(Throwable cause) {
        if (cause == null || cause.getMessage() == null) return -1;
        String msg = cause.getMessage();
        if (!msg.startsWith(TranscodeConstants.STEP_FAILED_PREFIX)) return -1;
        int prefixLen = TranscodeConstants.STEP_FAILED_PREFIX.length();
        int colon = msg.indexOf(':', prefixLen);
        if (colon <= prefixLen) return -1;
        try {
            return Integer.parseInt(msg.substring(prefixLen, colon));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static List<StepProgressItem> buildStepProgressListWithFailure(
            Map<Integer, StrategyStepVO> stepByStepId, List<Integer> stepIds, int completedCount,
            List<Integer> currentLevel, int failedStepId) {
        List<StepProgressItem> list = new ArrayList<>();
        for (int sid : stepIds) {
            StrategyStepVO step = stepByStepId.get(sid);
            String type = step != null ? (step.getType() != null ? step.getType() : "transcode") : "transcode";
            String name = stepTypeName(type) + " (步骤" + sid + ")";
            StepProgressItem item = new StepProgressItem();
            item.setStepId(sid);
            item.setType(type);
            item.setName(name);
            item.setDepends(step != null ? step.getDepends() : null);
            int idx = stepIds.indexOf(sid);
            if (idx < completedCount) {
                item.setStatus("completed");
                item.setProgress(100);
            } else if (sid == failedStepId) {
                item.setStatus("failed");
                item.setProgress(0);
            } else if (currentLevel.contains(sid)) {
                item.setStatus("processing");
                item.setProgress(0);
            } else {
                item.setStatus("pending");
                item.setProgress(0);
            }
            list.add(item);
        }
        return list;
    }

    /**
     * 在无依赖的叶子步骤中选取「主输出」步骤。
     * 多输出时优先选 transcode（转码视频），其次 sprite/extract_frames，避免雪碧图等图片被当作主输出。
     */
    private static int findLastStepByTopology(List<Integer> stepIds, Map<Integer, Integer[]> depsMap,
                                              Map<Integer, StrategyStepVO> stepByStepId) {
        Set<Integer> hasDependent = new HashSet<>();
        for (Integer sid : stepIds) {
            Integer[] deps = depsMap.get(sid);
            if (deps != null) hasDependent.addAll(Arrays.asList(deps));
        }
        List<Integer> leaves = new ArrayList<>();
        for (Integer sid : stepIds) {
            if (!hasDependent.contains(sid)) leaves.add(sid);
        }
        if (leaves.isEmpty()) return stepIds.getLast();
        // 优先选 transcode 作为主输出（视频文件更符合「主输出」预期），取 stepId 最小的（通常为 1080p 等主规格）
        int transcodeFirst = Integer.MAX_VALUE;
        for (int sid : leaves) {
            StrategyStepVO step = stepByStepId.get(sid);
            String type = step != null && step.getType() != null ? step.getType() : "";
            if ("transcode".equals(type)) transcodeFirst = Math.min(transcodeFirst, sid);
        }
        if (transcodeFirst < Integer.MAX_VALUE) return transcodeFirst;
        return leaves.stream().max(Integer::compareTo).orElse(stepIds.getLast());
    }

    private static List<StepProgressItem> buildStepProgressList(Map<Integer, StrategyStepVO> stepByStepId,
                                                               List<Integer> stepIds, int completedCount,
                                                               List<Integer> currentLevel, boolean levelDone) {
        Map<Integer, AtomicInteger> levelProgress = levelDone ? null : Collections.emptyMap();
        return buildStepProgressListWithLevelProgress(stepByStepId, stepIds, completedCount, currentLevel, levelProgress, levelDone);
    }

    private static List<StepProgressItem> buildStepProgressListWithLevelProgress(
            Map<Integer, StrategyStepVO> stepByStepId, List<Integer> stepIds, int completedCount,
            List<Integer> currentLevel, Map<Integer, AtomicInteger> levelProgress) {
        return buildStepProgressListWithLevelProgress(stepByStepId, stepIds, completedCount, currentLevel, levelProgress, false);
    }

    private static List<StepProgressItem> buildStepProgressListWithLevelProgress(
            Map<Integer, StrategyStepVO> stepByStepId, List<Integer> stepIds, int completedCount,
            List<Integer> currentLevel, Map<Integer, AtomicInteger> levelProgress, boolean levelDone) {
        List<StepProgressItem> list = new ArrayList<>();
        for (int sid : stepIds) {
            StrategyStepVO step = stepByStepId.get(sid);
            String type = step != null ? (step.getType() != null ? step.getType() : "transcode") : "transcode";
            String name = stepTypeName(type) + " (步骤" + sid + ")";
            StepProgressItem item = new StepProgressItem();
            item.setStepId(sid);
            item.setType(type);
            item.setName(name);
            item.setDepends(step != null ? step.getDepends() : null);
            int idx = stepIds.indexOf(sid);
            if (idx < completedCount) {
                item.setStatus("completed");
                item.setProgress(100);
            } else if (currentLevel.contains(sid)) {
                int pct = levelDone ? 100 : 0;
                if (!levelDone && levelProgress != null && !levelProgress.isEmpty()) {
                    AtomicInteger ai = levelProgress.get(sid);
                    if (ai != null) pct = ai.get();
                }
                item.setStatus(pct >= 100 ? "completed" : "processing");
                item.setProgress(pct);
            } else {
                item.setStatus("pending");
                item.setProgress(0);
            }
            list.add(item);
        }
        return list;
    }

    private static String stepTypeName(String type) {
        if (type == null) return "转码";
        return switch (type) {
            case "extract_frames" -> "抽帧";
            case "sprite" -> "雪碧图";
            case "image_convert" -> "图片转换";
            case "probe" -> "媒体分析";
            case "if" -> "判断";
            default -> "转码";
        };
    }

    private static List<List<Integer>> buildLevels(List<Integer> stepIds, Map<Integer, Integer[]> depsMap) {
        Set<Integer> done = new HashSet<>();
        List<List<Integer>> levels = new ArrayList<>();
        int n = stepIds.size();
        while (done.size() < n) {
            List<Integer> level = new ArrayList<>();
            for (Integer sid : stepIds) {
                if (done.contains(sid)) continue;
                Integer[] deps = depsMap.get(sid);
                if (deps == null) deps = new Integer[0];
                boolean allDone = true;
                for (int d : deps) {
                    if (!done.contains(d)) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) level.add(sid);
            }
            if (level.isEmpty()) throw new IllegalStateException("步骤依赖存在环或无效引用");
            levels.add(level);
            done.addAll(level);
        }
        return levels;
    }
}
