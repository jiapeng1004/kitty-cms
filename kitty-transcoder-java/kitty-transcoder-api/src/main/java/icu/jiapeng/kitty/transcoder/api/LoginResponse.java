package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @Schema(description = "API Token，请求时放在 Authorization: Bearer 或 X-Api-Token")
    private String token;

    @Schema(description = "当前登录的 Access Key ID")
    private String accessKeyId;
}
