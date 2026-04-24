package icu.jiapeng.kitty.material.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 栏目更新 DTO。
 * 移动/排序时通过 {@link #targetId} 与 {@link #position} 描述意图，其中 {@code inside} 表示新父级为目标（弹窗选父、拖入节点内均如此），不再单独传父栏目 id。
 */
@Data
@Schema(description = "栏目更新DTO")
public class CatalogUpdateDTO {
    @NotBlank(message = "栏目ID不能为空")
    @Schema(description = "栏目ID")
    private String id;

    @Schema(description = "栏目名称（可选）")
    private String name;

    @Schema(description = "同级排序值（可选，直接指定时与 targetId/position 互斥于「相对目标移动」类场景）")
    private Integer sortNum;

    @Schema(description = "目标栏目 id：与 position 成对。before/after 为参照节点；inside 为要挂入的父（或成为其子）")
    private String targetId;

    @Schema(description = "相对目标的落点，与 targetId 同时出现；INSIDE 时新父=目标，不再单独传父 id")
    private CatalogMovePosition position;
}
