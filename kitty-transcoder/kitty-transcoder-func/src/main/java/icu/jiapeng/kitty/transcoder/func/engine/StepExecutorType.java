package icu.jiapeng.kitty.transcoder.func.engine;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 步骤执行器类型枚举，与策略步骤的 type 字段一致，供各 StepExecutor 实例返回。
 */
@Getter
@AllArgsConstructor
public enum StepExecutorType {

    TRANSCODE("transcode", "转码（可选带水印）"),
    EXTRACT_FRAMES("extract_frames", "抽帧"),
    SPRITE("sprite", "雪碧图"),
    PROBE("probe", "媒体信息分析"),
    IF("if", "判断");

    private final String code;
    private final String desc;
}
