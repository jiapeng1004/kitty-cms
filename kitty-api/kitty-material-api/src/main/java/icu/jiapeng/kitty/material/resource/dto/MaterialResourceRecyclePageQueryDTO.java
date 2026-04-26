package icu.jiapeng.kitty.material.resource.dto;

import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.material.resource.vo.MaterialResourceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 回收站专用分页：仅按栏目、父级查 deleted=1 的资源，走数据库分页与 JDBC，不参与全文/向量检索。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "回收站资源分页查询")
public class MaterialResourceRecyclePageQueryDTO extends PageReqDTO<MaterialResourceVO> {

    @Schema(description = "栏目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String catalogId;

    @Schema(description = "父资源ID，根目录为 0；不传则按根目录")
    private String parentId;
}
