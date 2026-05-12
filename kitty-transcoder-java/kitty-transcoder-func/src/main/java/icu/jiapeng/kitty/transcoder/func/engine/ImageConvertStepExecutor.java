package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import jakarta.validation.constraints.NotBlank;
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
    public String execute(@NotBlank String inputPath, @NotBlank String outputPath, StrategyStepVO step) throws Exception {
        StepContext context = StepContext.getInstance();
        String stepWorkDir = context != null ? context.getWorkDir() : null;
        return mediaStepOps.doImageConvert(inputPath, step, outputPath, stepWorkDir);
    }
}
