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
    public String execute(String inputPath,String outputPath, StrategyStepVO step) throws Exception {
        StepContext context = StepContext.getInstance();
        String stepWorkDir = context != null ? context.getWorkDir() : null;
        return mediaStepOps.doExtractFrames(inputPath, step, outputPath, stepWorkDir, context);
    }
}
