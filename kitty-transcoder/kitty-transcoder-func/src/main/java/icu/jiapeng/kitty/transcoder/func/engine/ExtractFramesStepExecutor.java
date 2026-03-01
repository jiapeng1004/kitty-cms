package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.springframework.stereotype.Component;

@Component
public class ExtractFramesStepExecutor implements StepExecutor {

    private final MediaStepOps mediaStepOps;

    public ExtractFramesStepExecutor(MediaStepOps mediaStepOps) {
        this.mediaStepOps = mediaStepOps;
    }

    @Override
    public String getType() {
        return StepExecutorType.EXTRACT_FRAMES.getCode();
    }

    @Override
    public String execute(String inputPath, StrategyStepVO step, String stepSuffix, StepContext context) throws Exception {
        String outputPath = context != null ? context.getResolvedOutputPath() : null;
        return mediaStepOps.doExtractFrames(inputPath, step, stepSuffix, outputPath);
    }
}
