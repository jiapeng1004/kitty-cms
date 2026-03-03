package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StepProgressItem {

    @Schema(description = "步骤序号", example = "1")
    private Integer stepId;

    @Schema(description = "步骤类型", example = "transcode")
    private String type;

    @Schema(description = "步骤名称", example = "转码")
    private String name;

    @Schema(description = "状态：completed/processing/pending/failed", example = "processing")
    private String status;

    @Schema(description = "步骤内进度 0-100，completed=100", example = "0")
    private Integer progress;
}
