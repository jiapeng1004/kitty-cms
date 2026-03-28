package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目模板创建/更新")
public class MaterialMetadataTemplateUpsertDTO {

    @Schema(description = "模板ID（更新必填）")
    private String id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "绑定栏目ID")
    private String catalogId;

    @Schema(description = "绑定资源类型，空表示栏目通用模板")
    private Integer resourceType;

    @Schema(description = "是否启用 1/0")
    private Integer enabled;
}
