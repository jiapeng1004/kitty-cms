package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.springframework.stereotype.Component;

@Component
public class TranscodeStepExecutor implements StepExecutor {

    private final MediaStepOps mediaStepOps;

    public TranscodeStepExecutor(MediaStepOps mediaStepOps) {
        this.mediaStepOps = mediaStepOps;
    }

    @Override
    public String getType() {
        return StepExecutorType.TRANSCODE.getCode();
    }

    @Override
    public String execute(String inputPath, StrategyStepVO step, String stepSuffix, StepContext context) throws Exception {
        String outputPath = context != null ? context.getResolvedOutputPath() : null;
        String wmUrl = context != null ? context.getWatermarkUrl() : null;
        String wmPos = context != null ? context.getWatermarkPosition() : null;
        String taskId = context != null ? context.getTaskId() : null;
        String stepWorkDir = context != null ? context.getWorkDir() : null;
        return mediaStepOps.doTranscode(inputPath, step, stepSuffix, outputPath, wmUrl, wmPos, taskId, stepWorkDir, context);
    }
}