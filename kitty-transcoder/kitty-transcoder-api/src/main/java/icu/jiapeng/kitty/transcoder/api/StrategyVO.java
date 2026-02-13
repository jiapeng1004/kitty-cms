package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StrategyVO {

    @Schema(description = "策略ID", example = "strategy_001")
    private String id;

    @Schema(description = "策略名称", example = "高清转码")
    private String name;

    @Schema(description = "目标格式", example = "mp4")
    private String targetFormat;

    @Schema(description = "目标分辨率", example = "1920x1080")
    private String resolution;

    @Schema(description = "目标码率", example = "5000")
    private Integer bitrate;

    @Schema(description = "目标帧率", example = "30")
    private Integer frameRate;

    @Schema(description = "编码器", example = "h264")
    private String encoder;

    @Schema(description = "是否添加水印", example = "false")
    private Boolean addWatermark;

    @Schema(description = "水印位置", example = "bottom-right")
    private String watermarkPosition;

    @Schema(description = "水印路径", example = "/path/to/watermark.png")
    private String watermarkPath;

    @Schema(description = "创建时间", example = "1739457045000")
    private Long createdAt;
}