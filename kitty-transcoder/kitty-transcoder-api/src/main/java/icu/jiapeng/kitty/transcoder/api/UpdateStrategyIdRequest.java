package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStrategyIdRequest {

    @NotBlank(message = "新策略ID不能为空")
    @Schema(description = "新策略ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newId;
}
