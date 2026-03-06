package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationConfig {

    @NotBlank(message = "通知方式不能为空")
    @Schema(description = "通知方式：HTTP（目标为 URL）、GRPC（目标为 host:port，调用方需实现 TranscodeListenerService）", example = "HTTP")
    private String method;

    @NotBlank(message = "通知目标不能为空")
    @Size(max = 500, message = "通知目标长度不能超过500")
    @Schema(description = "通知目标：HTTP 时为回调 URL；GRPC 时为 host:port（如 192.168.1.100:9901）", example = "http://localhost:8080/callback")
    private String target;
}