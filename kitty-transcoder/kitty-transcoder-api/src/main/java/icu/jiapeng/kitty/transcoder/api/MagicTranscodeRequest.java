package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "同步单目标转码请求")
public class MagicTranscodeRequest {

    @Schema(description = "输入类型：DISK=本地路径，HTTP=URL", example = "DISK")
    private String inputType = "DISK";

    @Schema(description = "输入路径或 HTTP URL", required = true)
    private String inputPath;

    @Schema(description = "目标格式", example = "mp4")
    private String targetFormat = "mp4";

    @Schema(description = "分辨率", example = "1920x1080")
    private String resolution = "1920x1080";

    @Schema(description = "码率 kbps", example = "5000")
    private Integer bitrate = 5000;

    @Schema(description = "帧率", example = "30")
    private Integer frameRate = 30;
}
