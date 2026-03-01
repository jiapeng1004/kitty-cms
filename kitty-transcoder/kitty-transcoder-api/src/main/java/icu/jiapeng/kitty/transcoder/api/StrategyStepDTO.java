package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建/更新策略时的一个步骤参数。
 */
@Data
public class StrategyStepDTO {

    @Schema(description = "步骤类型：transcode=转码（可带水印），extract_frames=抽帧，sprite=雪碧图，probe=探测，if=条件分支", example = "transcode")
    private String type = "transcode";

    @Schema(description = "依赖的步骤序号，逗号分隔，如 \"0\"，空表示无依赖")
    private String depends;

    @Schema(description = "输入路径模板，支持 $TASK_ID、$DATE_TIME、$TASK_INPUT、$STEP_OUTPUT_0 等")
    private String inputTemplate;

    @Schema(description = "输出路径模板，支持 $TASK_ID、$DATE_TIME、$STEP_INDEX、$WORK_DIR 等")
    private String outputTemplate;

    @Schema(description = "目标格式", example = "mp4")
    private String targetFormat;

    @Schema(description = "目标分辨率", example = "1920x1080")
    private String resolution;

    @Schema(description = "目标码率 kbps", example = "5000")
    private Integer bitrate;

    @Schema(description = "帧率 fps", example = "30")
    private Integer frameRate;

    @Schema(description = "编码器", example = "h264")
    private String encoder;

    @Schema(description = "是否添加水印")
    private Boolean addWatermark;

    @Schema(description = "水印位置", example = "bottom-right")
    private String watermarkPosition;

    @Schema(description = "水印图片路径")
    private String watermarkPath;

    @Schema(description = "抽帧间隔，每隔多少帧取一帧（抽帧/雪碧图）", example = "30")
    private Integer frameInterval;

    @Schema(description = "抽帧数量，指定则抽指定帧数，不指定则默认 1 帧（仅抽帧步骤）")
    private Integer extractFrameCount;

    @Schema(description = "抽帧输出图片格式（抽帧/雪碧图）", example = "jpg")
    private String extractOutputFormat;

    @Schema(description = "雪碧图列数", example = "4")
    private Integer spriteColumns;

    @Schema(description = "雪碧图行数", example = "3")
    private Integer spriteRows;

    @Schema(description = "if 步骤：条件表达式，如 width>1920、bitrate<5000000")
    private String condition;

    @Schema(description = "if 步骤：条件为真时执行的策略 ID")
    private String strategyIdWhenTrue;

    @Schema(description = "if 步骤：条件为假时执行的策略 ID")
    private String strategyIdWhenFalse;
}
