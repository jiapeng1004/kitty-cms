package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

public class StepContextImpl implements StepContext {

    /** 并行步骤时，各线程通过 ThreadLocal 持有自己的 stepId，避免 reportStepProgress 读到被其他线程覆盖的值 */
    private static final ThreadLocal<Integer> currentStepIndexForThread = new ThreadLocal<>();

    private int currentStepIndex;
    private int inputStepIndex = -1;
    private String resolvedOutputPath;
    private String watermarkUrl;
    private String watermarkPosition;
    private String taskId;
    private String workDir;
    private final Map<Integer, ProbeResult> probeResults = new ConcurrentHashMap<>();
    private final RunStrategyCallback runStrategyCallback;
    private volatile BiConsumer<Integer, Integer> stepProgressReporter;
    private volatile BooleanSupplier cancellationChecker;

    public StepContextImpl(RunStrategyCallback runStrategyCallback) {
        this.runStrategyCallback = runStrategyCallback;
    }

    public void setStepProgressReporter(BiConsumer<Integer, Integer> reporter) {
        this.stepProgressReporter = reporter;
    }

    public void setCancellationChecker(BooleanSupplier checker) {
        this.cancellationChecker = checker;
    }

    @Override
    public boolean isCancelled() {
        return cancellationChecker != null && cancellationChecker.getAsBoolean();
    }

    @Override
    public void reportStepProgress(int percent) {
        BiConsumer<Integer, Integer> r = stepProgressReporter;
        if (r != null && percent >= 0 && percent <= 100) {
            Integer sid = currentStepIndexForThread.get();
            if (sid == null) sid = currentStepIndex;
            r.accept(sid, percent);
        }
    }

    @Override
    public void setCurrentStepIndex(int stepIndex) {
        this.currentStepIndex = stepIndex;
        currentStepIndexForThread.set(stepIndex);
    }

    /** 步骤结束时调用，清理当前线程的 ThreadLocal，避免线程复用后读到旧值 */
    public static void clearCurrentStepIndexForThread() {
        currentStepIndexForThread.remove();
    }
    @Override
    public int getCurrentStepIndex() { return currentStepIndex; }
    @Override
    public void setInputStepIndex(int stepIndex) { this.inputStepIndex = stepIndex; }
    @Override
    public int getInputStepIndex() { return inputStepIndex; }
    @Override
    public void setProbeResult(int stepIndex, ProbeResult result) {
        if (result != null) probeResults.put(stepIndex, result);
    }
    @Override
    public ProbeResult getProbeResult(int stepIndex) { return probeResults.get(stepIndex); }
    @Override
    public void setResolvedOutputPath(String path) { this.resolvedOutputPath = path; }
    @Override
    public String getResolvedOutputPath() { return resolvedOutputPath; }
    @Override
    public String runStrategy(String strategyId, String inputPath) throws Exception {
        return runStrategyCallback.runStrategy(strategyId, inputPath);
    }
    @Override
    public String getWatermarkUrl() { return watermarkUrl; }
    @Override
    public void setWatermarkUrl(String url) { this.watermarkUrl = url; }
    @Override
    public String getWatermarkPosition() { return watermarkPosition; }
    @Override
    public void setWatermarkPosition(String position) { this.watermarkPosition = position; }
    @Override
    public String getTaskId() { return taskId; }
    @Override
    public void setTaskId(String id) { this.taskId = id; }
    @Override
    public String getWorkDir() { return workDir; }
    @Override
    public void setWorkDir(String workDir) { this.workDir = workDir; }

    @FunctionalInterface
    public interface RunStrategyCallback {
        String runStrategy(String strategyId, String inputPath) throws Exception;
    }
}
