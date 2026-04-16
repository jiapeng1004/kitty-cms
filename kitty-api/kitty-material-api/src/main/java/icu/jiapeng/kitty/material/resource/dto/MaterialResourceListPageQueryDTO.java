package icu.jiapeng.kitty.material.resource.dto;

import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源分页列表查询（栏目 + 父级；可选 keyword / semanticText 时先走与 {@link MaterialResourceListQueryDTO#list} 相同逻辑再内存切片，总条数受检索上限约束）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "资源分页列表查询参数")
public class MaterialResourceListPageQueryDTO extends PageReqDTO<MaterialResourceVO> {

    @Schema(description = "栏目ID")
    private String catalogId;

    @Schema(description = "父资源ID，根目录为 0；不传则不过滤父级")
    private String parentId;

    @Schema(description = "关键词（与 list 一致：全文检索链路）")
    private String keyword;

    @Schema(description = "语义检索文本（与 list 一致）")
    private String semanticText;
}
