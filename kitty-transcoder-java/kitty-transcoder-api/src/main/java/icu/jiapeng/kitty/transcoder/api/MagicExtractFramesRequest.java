package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "同步抽帧请求")
public class MagicExtractFramesRequest {

    @Schema(description = "输入类型：DISK=本地路径，HTTP=URL", example = "DISK")
    private String inputType = "DISK";

    @Schema(description = "输入路径或 HTTP URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inputPath;

    @Schema(description = "抽帧间隔（每隔多少帧取一帧）", example = "30")
    private Integer frameInterval = 30;

    @Schema(description = "抽帧数量", example = "1")
    private Integer frameCount = 1;

    @Schema(description = "输出格式：jpg 或 png", example = "jpg")
    private String outputFormat = "jpg";
}
