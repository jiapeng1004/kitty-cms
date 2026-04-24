package icu.jiapeng.kitty.material.transcode.model;

import icu.jiapeng.kitty.material.transcode.entity.KtMaterialTranscodeStrategy;

import java.util.Optional;

public record TranscodeStrategyResolution(Optional<KtMaterialTranscodeStrategy> strategy,
                                          TranscodeStrategySource source) {

    public static TranscodeStrategyResolution none() {
        return new TranscodeStrategyResolution(Optional.empty(), TranscodeStrategySource.NONE);
    }

    public static TranscodeStrategyResolution of(KtMaterialTranscodeStrategy strategy, TranscodeStrategySource source) {
        return new TranscodeStrategyResolution(Optional.ofNullable(strategy), source);
    }
}
