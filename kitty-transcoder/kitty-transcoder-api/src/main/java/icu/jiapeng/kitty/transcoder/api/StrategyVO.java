package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@FieldNameConstants
public class StrategyVO {

    @Schema(description = "策略ID（root_id）")
    private Long id;

    @Schema(description = "策略名称", example = "高清转码")
    private String name;

    @Schema(description = "步骤数（列表用）")
    private Integer stepCount;

    @Schema(description = "步骤列表（详情用），按 stepId 顺序执行，如先 1080p 再 2160p 再加水印")
    private List<StrategyStepVO> steps;

    @Schema(description = "策略级工作目录，本策略下输入/输出与 HTTP 下载均限定在此；空则用全局 transcoder.work-dir")
    private String workDir;

    @Schema(description = "创建时间", example = "1739457045000")
    private Long createdAt;

    // 兼容单步骤展示（仅当 steps 为空或列表项时使用）
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
}