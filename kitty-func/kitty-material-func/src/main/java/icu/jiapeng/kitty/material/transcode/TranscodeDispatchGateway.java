package icu.jiapeng.kitty.material.transcode;

import java.util.Optional;

/**
 * 8.1 转码平台适配端口：下发异步任务，返回外部任务 ID。
 */
public interface TranscodeDispatchGateway {

    /**
     * @param platformCode 素材侧策略上的 {@link icu.jiapeng.kitty.material.transcode.entity.MaterialTranscodeStrategy#getPlatformCode()}
     */
    Optional<String> submit(String platformCode, TranscodeSubmitCommand command);
}
