package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单一步骤的输出，用于回调中返回多路转码/抽帧/雪碧图等全部输出。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "步骤输出项")
public class StepOutputItem {

    @Schema(description = "步骤 ID", example = "1")
    private Integer stepId;

    @Schema(description = "步骤类型：transcode, extract_frames, sprite 等", example = "transcode")
    private String stepType;

    @Schema(description = "本地输出路径")
    private String outputPath;

    @Schema(description = "输出 HTTP 访问 URL")
    private String outputHttpUrl;
}
