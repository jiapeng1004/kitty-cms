package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TranscodeEngine {

    @Resource
    private StrategyService strategyService;

    @Resource
    private TranscodeConfig transcodeConfig;

    private final ExecutorService stepExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public interface ProgressCallback {
        void updateProgress(String taskId, String status, int progress);
    }

    public String transcode(String taskId, String inputFile, String strategyId, ProgressCallback progressCallback) throws Exception {
        StrategyVO strategy = strategyService.getStrategy(strategyId);
        if (strategy == null || strategy.getSteps() == null || strategy.getSteps().isEmpty()) {
            StrategyStepVO defaultStep = new StrategyStepVO();
            defaultStep.setType(StepExecutorType.TRANSCODE.getCode());
            defaultStep.setTargetFormat("mp4");
            defaultStep.setResolution("1920x1080");
            defaultStep.setBitrate(5000);
            defaultStep.setFrameRate(30);
            defaultStep.setEncoder("h264");
            StepExecutor exec = StepExecutor.Factory.resolveOrFail(StepExecutorType.TRANSCODE.getCode());
            return exec.execute(inputFile, defaultStep, "_transcoded", null);
        }
        return runWithDependencies(taskId, inputFile, strategy, progressCallback);
    }

    private String runWithDependencies(String taskId, String taskInputPath, StrategyVO strategy, ProgressCallback progressCallback) throws Exception {
        List<StrategyStepVO> steps = strategy.getSteps();
        int n = steps.size();
        Map<Integer, int[]> depsMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            // 依赖不声明 = 无依赖，使用最上层任务输入，可与其它无依赖步骤并行
            depsMap.put(i, parseDepends(steps.get(i).getDepends()));
        }

        StepContextImpl.RunStrategyCallback runStrategyCallback = (strategyId, inputPath) -> {
            StrategyVO sub = strategyService.getStrategy(strategyId);
            if (sub == null) throw new IllegalArgumentException("策略不存在：" + strategyId);
            return runWithDependencies(taskId, inputPath, sub, progressCallback);
        };
        StepContextImpl ctx = new StepContextImpl(runStrategyCallback);

        List<List<Integer>> levels = buildLevels(n, depsMap);
        Map<Integer, String> stepOutputs = new ConcurrentHashMap<>();

        String workDir = transcodeConfig.getTempDir();
        int completed = 0;
        for (List<Integer> level : levels) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int idx : level) {
                StrategyStepVO step = steps.get(idx);
                int[] deps = depsMap.get(idx);
                String inputPath;
                if (step.getInputTemplate() != null && !step.getInputTemplate().isBlank()) {
                    inputPath = StepTemplateResolver.resolve(step.getInputTemplate(), taskId, taskInputPath, stepOutputs, idx, workDir);
                } else {
                    inputPath = deps.length == 0 ? taskInputPath : stepOutputs.get(maxOf(deps));
                }
                if (inputPath == null || inputPath.isBlank()) throw new IllegalStateException("步骤 " + idx + " 输入路径为空");
                String resolvedOutputPath = null;
                if (step.getOutputTemplate() != null && !step.getOutputTemplate().isBlank()) {
                    resolvedOutputPath = StepTemplateResolver.resolve(step.getOutputTemplate(), taskId, taskInputPath, stepOutputs, idx, workDir);
                }
                int inputStepIndex = deps.length == 0 ? -1 : maxOf(deps);
                String stepSuffix = "_s" + idx;
                ctx.setCurrentStepIndex(idx);
                ctx.setInputStepIndex(inputStepIndex);
                ctx.setResolvedOutputPath(resolvedOutputPath);
                StepExecutor exec = StepExecutor.Factory.resolveOrFail(step.getType());
                final String inp = inputPath;
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    try {
                        String out = exec.execute(inp, step, stepSuffix, ctx);
                        stepOutputs.put(idx, out);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, stepExecutor);
                futures.add(f);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            completed += level.size();
            if (progressCallback != null) {
                progressCallback.updateProgress(taskId, "PROCESSING", (completed * 100) / n);
            }
        }

        int lastStep = findLastStepByTopology(n, depsMap);
        String lastOutput = stepOutputs.get(lastStep);
        if (lastOutput == null) throw new IllegalStateException("无最终输出");
        return lastOutput;
    }

    private static int maxOf(int[] a) {
        if (a.length == 0) return -1;
        int m = a[0];
        for (int i = 1; i < a.length; i++) if (a[i] > m) m = a[i];
        return m;
    }

    private static int[] parseDepends(String depends) {
        if (depends == null || depends.isBlank()) return new int[0];
        String[] parts = depends.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            out[i] = Integer.parseInt(parts[i].trim());
        }
        return out;
    }

    private static int findLastStepByTopology(int n, Map<Integer, int[]> depsMap) {
        Set<Integer> hasDependent = new HashSet<>();
        for (int i = 0; i < n; i++) {
            for (int d : depsMap.get(i)) hasDependent.add(d);
        }
        int last = -1;
        for (int i = 0; i < n; i++) {
            if (!hasDependent.contains(i)) last = Math.max(last, i);
        }
        return last >= 0 ? last : n - 1;
    }

    private static List<List<Integer>> buildLevels(int n, Map<Integer, int[]> depsMap) {
        Set<Integer> done = new HashSet<>();
        List<List<Integer>> levels = new ArrayList<>();
        while (done.size() < n) {
            List<Integer> level = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (done.contains(i)) continue;
                int[] deps = depsMap.get(i);
                boolean allDone = true;
                for (int d : deps) {
                    if (!done.contains(d)) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) level.add(i);
            }
            if (level.isEmpty()) throw new IllegalStateException("步骤依赖存在环或无效引用");
            levels.add(level);
            done.addAll(level);
        }
        return levels;
    }
}
