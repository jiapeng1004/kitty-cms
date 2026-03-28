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
}
