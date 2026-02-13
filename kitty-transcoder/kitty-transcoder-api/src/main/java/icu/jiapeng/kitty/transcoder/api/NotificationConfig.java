package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationConfig {

    @NotBlank(message = "通知方式不能为空")
    @Schema(description = "通知方式（MQ、HTTP、WebSocket）", example = "MQ")
    private String method;

    @NotBlank(message = "通知目标不能为空")
    @Size(max = 500, message = "通知目标长度不能超过500")
    @Schema(description = "通知目标（队列名、URL等）", example = "transcode.notifications")
    private String target;
}