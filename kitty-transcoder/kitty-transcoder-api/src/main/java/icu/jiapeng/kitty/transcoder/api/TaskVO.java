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

    @Schema(description = "输入文件路径", example = "/path/to/video.mp4")
    private String inputFile;

    @Schema(description = "输出文件路径", example = "/path/to/output.mp4")
    private String outputFile;

    @Schema(description = "策略ID", example = "strategy_001")
    private String strategyId;

    @Schema(description = "创建时间", example = "1739457045000")
    private Long createdAt;

    @Schema(description = "开始时间", example = "1739457050000")
    private Long startedAt;

    @Schema(description = "完成时间", example = "1739457345000")
    private Long completedAt;

    @Schema(description = "错误信息", example = "转码失败：FFmpeg 执行错误")
    private String errorMessage;
}