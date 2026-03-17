package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {

    @Schema(description = "输入类型：DISK 磁盘路径 / HTTP 地址", example = "DISK", allowableValues = {"DISK", "HTTP"})
    private String inputType = "DISK";

    @NotBlank(message = "输入不能为空")
    @Size(max = 1024, message = "输入路径或URL长度不能超过1024")
    @Schema(description = "输入：磁盘路径或 HTTP URL（根据 inputType）", example = "/path/to/video.mp4")
    private String inputPath;

    /** @deprecated 使用 inputPath */
    @Schema(hidden = true)
    private String inputFile;

    @Schema(description = "转码策略ID（root_id）", example = "22", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyId;

    @Valid
    @Schema(description = "通知配置")
    private List<NotificationConfig> notifications;

    @Schema(description = "任务优先级 1-10", example = "5")
    private Integer priority = 5;

    @Schema(description = "水印地址（可选，传则转码时叠加水印；支持本地路径或 HTTP URL）", example = "/path/to/watermark.png")
    private String watermarkUrl;

    @Schema(description = "水印位置（可选）", example = "bottom-right", allowableValues = {"top-left", "top-right", "bottom-left", "bottom-right"})
    private String watermarkPosition;
}