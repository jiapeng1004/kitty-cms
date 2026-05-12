package icu.jiapeng.kitty.transcoder.func.engine;

import java.util.Map;

/**
 * 转码执行结果：主输出路径 + 各步骤输出路径（用于步骤级重试）。
 */
public record TranscodeResult(String mainOutput, Map<Integer, String> stepOutputs) {
}
