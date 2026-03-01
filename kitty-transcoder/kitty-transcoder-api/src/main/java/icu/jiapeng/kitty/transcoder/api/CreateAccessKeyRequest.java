package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateAccessKeyRequest {

    @Schema(description = "Access Key 名称", example = "前端控制台")
    private String name;
}
