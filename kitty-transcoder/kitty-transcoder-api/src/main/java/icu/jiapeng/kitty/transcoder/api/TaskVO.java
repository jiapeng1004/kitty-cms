package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TaskVO {

    @Schema(description = "任务ID", example = "task_123456")
    private String id;

    @Schema(description = "任务状态", example = "PROCESSING")
    private String status;

    @Schema(description = "转码进度", example = "50")
    private Integer progress;

    @Schema(description = "输入类型 DISK/HTTP")
    private String inputType;

    @Schema(description = "输入路径或URL", example = "/path/to/video.mp4")
    private String inputPath;

    @Schema(description = "输入文件路径（兼容）", example = "/path/to/video.mp4")
    private String inputFile;

    @Schema(description = "输出磁盘路径", example = "/path/to/output.mp4")
    private String outputPath;

    @Schema(description = "输出文件路径（兼容）", example = "/path/to/output.mp4")
    private String outputFile;

    @Schema(description = "输出 HTTP 地址")
    private String outputHttpUrl;

    @Schema(description = "策略ID", example = "strategy_001")
    private String strategyId;

    @Schema(description = "水印地址（创建时传入）")
    private String watermarkUrl;
    @Schema(description = "水印位置")
    private String watermarkPosition;

    @Schema(description = "创建时间", example = "1739457045000")
    private Long createdAt;

    @Schema(description = "开始时间", example = "1739457050000")
    private Long startedAt;

    @Schema(description = "完成时间", example = "1739457345000")
    private Long completedAt;

    @Schema(description = "错误信息", example = "转码失败：FFmpeg 执行错误")
    private String errorMessage;
}