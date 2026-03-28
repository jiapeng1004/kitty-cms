package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源列表查询参数。
 */
@Data
@Schema(description = "资源列表查询参数")
public class MaterialResourceListQueryDTO {

    @Schema(description = "栏目ID")
    private String catalogId;

    @Schema(description = "父资源ID，根目录固定为0")
    private String parentId;

    @Schema(description = "关键词全文检索（非空则走 ES，与栏目/父级组合过滤；未启用 ES 时退化为内存标题包含）")
    private String keyword;

    @Schema(description = "语义检索文本（非空则先向量化再走 ES kNN；与 keyword 同时存在时优先语义）")
    private String semanticText;

    @Schema(description = "检索最大条数，默认 20，最大 200")
    private Integer limit;
}
