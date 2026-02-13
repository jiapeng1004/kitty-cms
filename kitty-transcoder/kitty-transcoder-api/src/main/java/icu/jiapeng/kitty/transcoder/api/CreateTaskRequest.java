package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "输入文件不能为空")
    @Size(max = 500, message = "输入文件路径长度不能超过500")
    @Schema(description = "输入文件路径", example = "/path/to/video.mp4")
    private String inputFile;

    @NotBlank(message = "策略ID不能为空")
    @Size(max = 100, message = "策略ID长度不能超过100")
    @Schema(description = "转码策略ID", example = "strategy_001")
    private String strategyId;

    @Valid
    @Schema(description = "通知配置")
    private List<NotificationConfig> notifications;

    @Schema(description = "任务优先级", example = "1")
    private Integer priority;
}