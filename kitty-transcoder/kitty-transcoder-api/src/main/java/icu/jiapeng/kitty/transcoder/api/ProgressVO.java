package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProgressVO {

    @Schema(description = "任务ID", example = "task_123456")
    private String taskId;

    @Schema(description = "转码进度（0-100）", example = "50")
    private Integer progress;

    @Schema(description = "当前状态", example = "PROCESSING")
    private String status;

    @Schema(description = "处理时间（秒）", example = "15")
    private Integer processingTime;

    @Schema(description = "当前步骤名称", example = "转码")
    private String currentStep;

    @Schema(description = "总步骤数", example = "3")
    private Integer totalSteps;

    @Schema(description = "各步骤进度列表")
    private List<StepProgressItem> stepProgressList;
}