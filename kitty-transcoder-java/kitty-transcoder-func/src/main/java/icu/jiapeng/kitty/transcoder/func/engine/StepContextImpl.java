package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepContextImpl implements StepContext {

    /**
     * 并行步骤时，各线程通过 InheritableThreadLocal 持有自己的 stepId，
     * 避免 reportStepProgress 读到被其他线程覆盖的值；子线程（如 ffmpeg 进度线程）
     * 会继承创建时父线程的 stepId，从而将进度正确归属到对应步骤。
     */
    private static final InheritableThreadLocal<Integer> currentStepIndexForThread = new InheritableThreadLocal<>();

    private int currentStepIndex;
    private int inputStepIndex = -1;
    private String watermarkUrl;
    private String watermarkPosition;
    private String taskId;
    private String workDir;
    private Map<Integer, ProbeResult> probeResults = new ConcurrentHashMap<>();
    private RunStrategyCallback runStrategyCallback;
    private volatile BiConsumer<Integer, Integer> stepProgressReporter;
    private volatile BooleanSupplier cancellationChecker;

    public StepContextImpl(RunStrategyCallback runStrategyCallback) {
        this.runStrategyCallback = runStrategyCallback;
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

    /**
     * 步骤结束时调用，清理当前线程的 ThreadLocal，避免线程复用后读到旧值
     */
    public static void clearCurrentStepIndexForThread() {
        currentStepIndexForThread.remove();
    }

    @Override
    public void setProbeResult(int stepIndex, ProbeResult result) {
        if (result != null) probeResults.put(stepIndex, result);
    }

    @Override
    public ProbeResult getProbeResult(int stepIndex) {
        return probeResults.get(stepIndex);
    }


    @Override
    public String runStrategy(String strategyId, String inputPath) throws Exception {
        return runStrategyCallback.runStrategy(strategyId, inputPath);
    }


    @FunctionalInterface
    public interface RunStrategyCallback {
        String runStrategy(String strategyId, String inputPath) throws Exception;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        StepContextImpl clone = (StepContextImpl) super.clone();
        clone.setProbeResults(new HashMap<>(clone.getProbeResults()));
        return clone;
    }
}
