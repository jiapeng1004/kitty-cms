package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 转码进度通知统一载荷，HTTP 与 gRPC 回调共用。
 * 任务结束时通过 outputs 返回所有步骤输出（多路转码、抽帧、雪碧图等）。
 */
@Data
@Schema(description = "转码进度通知载荷")
public class TranscodeProgressNotifyVO {

    @Schema(description = "任务 ID")
    private String taskId;

    @Schema(description = "状态：PROCESSING, COMPLETED, FAILED, CANCELLED")
    private String status;

    @Schema(description = "进度 0-100")
    private Integer progress;

    @Schema(description = "主输出路径（完成时，与策略主输出步骤一致）")
    private String outputPath;

    @Schema(description = "主输出 HTTP 访问 URL（完成时）")
    private String outputHttpUrl;

    @Schema(description = "错误信息（失败时）")
    private String errorMessage;

    @Schema(description = "全部步骤输出（完成时）：多路转码、抽帧、雪碧图等，按 stepId 顺序")
    private List<StepOutputItem> outputs;
}
