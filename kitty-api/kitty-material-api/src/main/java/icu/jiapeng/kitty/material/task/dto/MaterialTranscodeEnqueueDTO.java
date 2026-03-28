package icu.jiapeng.kitty.material.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "手动/自动入队转码（input 为空时尝试用配置+meta_file 拼 HTTP URL）")
public class MaterialTranscodeEnqueueDTO {

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "DISK / HTTP，空默认 HTTP")
    private String inputType;

    @Schema(description = "入参路径或 URL")
    private String inputPath;

    @Schema(description = "覆盖选用素材侧策略ID（空则按栏目绑定规则匹配）")
    private String strategyId;

    @Schema(description = "优先级 1-10，默认 5")
    private Integer priority;
}
