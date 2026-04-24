package icu.jiapeng.kitty.material.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 栏目相对目标节点的落点（与 {@link CatalogUpdateDTO#getTargetId()} 成对使用）。
 * JSON 为枚举名：BEFORE / AFTER / INSIDE（与 Spring 默认反序列化一致）。
 */
@Schema(
        description = "相对目标的落点：BEFORE/AFTER 为与目标同一父下前后；INSIDE 为成为目标的子级（新父=目标），弹窗选父时亦用此项",
        allowableValues = {"BEFORE", "AFTER", "INSIDE"}
)
public enum CatalogMovePosition {
    @Schema(description = "置于目标同父兄弟中的目标之前")
    BEFORE,
    @Schema(description = "置于目标同父兄弟中的目标之后")
    AFTER,
    @Schema(description = "成为目标节点的子栏目；弹窗「移动到某父下」时选父后传此项")
    INSIDE
}
