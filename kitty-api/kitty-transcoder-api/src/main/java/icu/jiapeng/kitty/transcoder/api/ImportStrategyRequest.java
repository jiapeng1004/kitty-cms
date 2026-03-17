package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImportStrategyRequest {

    @NotBlank(message = "导入内容不能为空")
    @Schema(description = "策略文件内容（YAML 或 JSON 格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
