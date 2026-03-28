package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目模板查询")
public class MaterialMetadataTemplateQueryDTO {

    @Schema(description = "栏目ID（可空表示不限）")
    private String catalogId;

    @Schema(description = "资源类型（可空）")
    private Integer resourceType;

    @Schema(description = "仅返回启用模板")
    private Boolean enabledOnly;
}
