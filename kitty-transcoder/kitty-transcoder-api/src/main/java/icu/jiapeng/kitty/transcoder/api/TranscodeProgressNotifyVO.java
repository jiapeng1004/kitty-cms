package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 转码进度通知统一载荷，HTTP 与 gRPC 回调共用。
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

    @Schema(description = "输出路径（完成时）")
    private String outputPath;

    @Schema(description = "输出 HTTP 访问 URL（完成时）")
    private String outputHttpUrl;

    @Schema(description = "错误信息（失败时）")
    private String errorMessage;
}
