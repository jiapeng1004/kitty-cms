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
    public String execute(String inputPath, String outputPath, StrategyStepVO step) throws Exception {
        return mediaStepOps.doSpriteSheet(inputPath, outputPath, step);
    }
}
