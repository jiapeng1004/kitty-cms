package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "accessKeyId 不能为空")
    @Schema(description = "Access Key ID", example = "AKxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessKeyId;

    @NotBlank(message = "secretKey 不能为空")
    @Schema(description = "Secret Key", example = "SKxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String secretKey;
}
