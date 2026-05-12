package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 策略中的一个步骤（转码 / 水印等），步骤按 stepId 顺序执行。
 */
@Data
public class StrategyStepVO {

    @Schema(description = "步骤序号，从 1 开始")
    private Integer stepId;

    @Schema(description = "步骤类型：transcode=转码（可带水印），extract_frames=抽帧，sprite=雪碧图，probe=探测，if=条件分支")
    private String type;

    @Schema(description = "依赖的步骤序号，逗号分隔，从 1 开始，如 \"1\" 或 \"1,2\"，空表示无依赖")
    private String depends;

    @Schema(description = "输入路径模板，支持 $TASK_ID、$DATE（年/月/日）、$DATE_TIME、$TASK_INPUT、$STEP_OUTPUT_1 等；留空则按依赖推导")
    private String inputTemplate;

    @Schema(description = "输出路径模板，支持 $WORK_DIR、$DATE（年/月/日）、$DATE_TIME（年/月/日/时分秒）、$TASK_ID、$STEP_INDEX 等；留空则由执行器默认生成")
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

    @Schema(description = "抽帧间隔，每隔多少帧取一帧（仅抽帧步骤）", example = "30")
    private Integer frameInterval;

    @Schema(description = "抽帧数量，指定则抽指定帧数，不指定则默认 1 帧（仅抽帧步骤）")
    private Integer extractFrameCount;

    @Schema(description = "抽帧/雪碧图输出图片格式", example = "jpg")
    private String extractOutputFormat;

    @Schema(description = "雪碧图列数", example = "4")
    private Integer spriteColumns;

    @Schema(description = "雪碧图行数", example = "3")
    private Integer spriteRows;

    @Schema(description = "雪碧图缩放倍数，画面等比缩小到 1/N，默认 4 即 iw/4:ih/4", example = "4")
    private Integer spriteScale;

    @Schema(description = "图片转换目标格式（image_convert）", example = "webp")
    private String imageTargetFormat;

    @Schema(description = "图片质量 1-100（image_convert，jpg/webp 等）", example = "85")
    private Integer imageQuality;

    @Schema(description = "图片缩放，如 800x600、800x、x600（image_convert）")
    private String imageResize;

    @Schema(description = "if 步骤：条件表达式，如 width>1920、bitrate<5000000")
    private String condition;

    @Schema(description = "if 步骤：条件为真时执行的策略 ID")
    private String strategyIdWhenTrue;

    @Schema(description = "if 步骤：条件为假时执行的策略 ID")
    private String strategyIdWhenFalse;
}
