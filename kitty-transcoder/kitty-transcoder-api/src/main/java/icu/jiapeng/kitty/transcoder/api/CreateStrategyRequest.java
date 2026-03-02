package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateStrategyRequest {

    @NotBlank(message = "策略名称不能为空")
    @Size(max = 100, message = "策略名称长度不能超过100")
    @Schema(description = "策略名称", example = "高清转码")
    private String name;

    @Schema(description = "策略级工作目录，本策略下输入/输出与 HTTP 下载均限定在此；空则用全局 transcoder.work-dir")
    private String workDir;

    @Valid
    @Schema(description = "步骤列表，按顺序执行，如先转 1080p 再转 2160p 再加水印；空则视为单步转码")
    private List<StrategyStepDTO> steps;
}