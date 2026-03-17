package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "同步 ImageMagick 图转请求")
public class MagicImageConvertRequest {

    @Schema(description = "输入类型：DISK=本地路径，HTTP=URL", example = "DISK")
    private String inputType = "DISK";

    @Schema(description = "输入路径或 HTTP URL", required = true)
    private String inputPath;

    @Schema(description = "目标格式", example = "webp")
    private String targetFormat = "webp";

    @Schema(description = "质量 1-100（jpg/webp）", example = "85")
    private Integer quality = 85;

    @Schema(description = "缩放，如 800x600、800x、x600")
    private String resize;
}
