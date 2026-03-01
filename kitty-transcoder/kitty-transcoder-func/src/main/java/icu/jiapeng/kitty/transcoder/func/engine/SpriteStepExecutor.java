package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.springframework.stereotype.Component;

@Component
public class SpriteStepExecutor implements StepExecutor {

    private final MediaStepOps mediaStepOps;

    public SpriteStepExecutor(MediaStepOps mediaStepOps) {
        this.mediaStepOps = mediaStepOps;
    }

    @Override
    public String getType() {
        return StepExecutorType.SPRITE.getCode();
    }

    @Override
    public String execute(String inputPath, StrategyStepVO step, String stepSuffix, StepContext context) throws Exception {
        String outputPath = context != null ? context.getResolvedOutputPath() : null;
        return mediaStepOps.doSpriteSheet(inputPath, step, stepSuffix, outputPath);
    }
}
