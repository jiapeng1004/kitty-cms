package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.springframework.stereotype.Component;

@Component
public class ImageConvertStepExecutor implements StepExecutor {

    private final MediaStepOps mediaStepOps;

    public ImageConvertStepExecutor(MediaStepOps mediaStepOps) {
        this.mediaStepOps = mediaStepOps;
    }

    @Override
    public String getType() {
        return StepExecutorType.IMAGE_CONVERT.getCode();
    }

    @Override
    public String execute(String inputPath, StrategyStepVO step, String stepSuffix, StepContext context) throws Exception {
        String outputPath = context != null ? context.getResolvedOutputPath() : null;
        String stepWorkDir = context != null ? context.getWorkDir() : null;
        return mediaStepOps.doImageConvert(inputPath, step, stepSuffix, outputPath, stepWorkDir);
    }
}
