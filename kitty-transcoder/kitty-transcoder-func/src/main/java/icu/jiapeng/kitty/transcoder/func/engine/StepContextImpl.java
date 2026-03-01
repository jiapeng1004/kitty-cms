package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StepContextImpl implements StepContext {

    private int currentStepIndex;
    private int inputStepIndex = -1;
    private String resolvedOutputPath;
    private final Map<Integer, ProbeResult> probeResults = new ConcurrentHashMap<>();
    private final RunStrategyCallback runStrategyCallback;

    public StepContextImpl(RunStrategyCallback runStrategyCallback) {
        this.runStrategyCallback = runStrategyCallback;
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

    @FunctionalInterface
    public interface RunStrategyCallback {
        String runStrategy(String strategyId, String inputPath) throws Exception;
    }
}
