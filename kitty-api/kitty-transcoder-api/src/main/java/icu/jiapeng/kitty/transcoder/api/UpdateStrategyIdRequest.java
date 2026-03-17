package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStrategyIdRequest {

    @Schema(description = "新策略ID（root_id）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long newId;
}
