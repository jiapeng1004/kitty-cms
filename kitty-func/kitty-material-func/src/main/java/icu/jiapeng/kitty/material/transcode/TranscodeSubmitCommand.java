package icu.jiapeng.kitty.material.transcode;

import lombok.Builder;
import lombok.Value;

/**
 * 8.1 向转码平台创建异步任务所需最小参数（与 kitty-transcoder gRPC CreateTask 对齐）。
 */
@Value
@Builder
public class TranscodeSubmitCommand {

    String inputType;
    String inputPath;
    long externalStrategyId;
    int priority;
    /**
     * 与素材侧策略 params_json 对齐，经 gRPC 透传至转码服务（需服务端支持）。
     */
    String extraParamsJson;
}
