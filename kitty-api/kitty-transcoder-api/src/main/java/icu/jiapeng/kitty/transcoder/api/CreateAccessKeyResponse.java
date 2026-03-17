package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccessKeyResponse {

    @Schema(description = "Access Key ID")
    private String accessKeyId;

    @Schema(description = "Secret Key（仅创建时返回一次，请妥善保存）")
    private String secretKey;

    @Schema(description = "名称")
    private String name;
}
