package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源新增/更新参数。
 */
@Data
@Schema(description = "资源新增/更新参数")
public class MaterialResourceUpsertDTO {

    @Schema(description = "资源ID（更新时必填）")
    private String id;

    @Schema(description = "资源标题")
    private String title;

    @Schema(description = "栏目ID，必填")
    private String catalogId;

    @Schema(description = "父资源ID，默认0")
    private String parentId;

    @Schema(description = "资源类型")
    private Integer type;
}
