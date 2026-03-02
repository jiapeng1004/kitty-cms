package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class StepContextImpl implements StepContext {

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

    public StepContextImpl(RunStrategyCallback runStrategyCallback) {
        this.runStrategyCallback = runStrategyCallback;
    }

    public void setStepProgressReporter(BiConsumer<Integer, Integer> reporter) {
        this.stepProgressReporter = reporter;
    }

    @Override
    public void reportStepProgress(int percent) {
        BiConsumer<Integer, Integer> r = stepProgressReporter;
        if (r != null && percent >= 0 && percent <= 100) {
            r.accept(currentStepIndex, percent);
        }
    }

    @Override
    public void setCurrentStepIndex(int stepIndex) { this.currentStepIndex = stepIndex; }
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
