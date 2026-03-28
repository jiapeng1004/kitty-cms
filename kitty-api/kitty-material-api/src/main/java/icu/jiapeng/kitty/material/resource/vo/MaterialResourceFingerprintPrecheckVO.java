package icu.jiapeng.kitty.material.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 资源指纹秒传/排重预检结果。
 */
@Data
@Schema(description = "资源指纹秒传/排重预检结果")
public class MaterialResourceFingerprintPrecheckVO {
    @Schema(description = "是否命中可秒传")
    private Boolean hit;

    @Schema(description = "匹配到的资源")
    private List<MaterialResourceVO> matchedResources;
}
