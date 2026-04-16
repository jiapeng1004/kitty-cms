package icu.jiapeng.kitty.material.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 栏目更新 DTO。
 * 支持通过不同字段实现重命名、调整父级（移动）、调整排序。
 */
@Data
@Schema(description = "栏目更新DTO")
public class CatalogUpdateDTO {
    @NotBlank(message = "栏目ID不能为空")
    @Schema(description = "栏目ID")
    private String id;

    @Schema(description = "栏目名称（可选）")
    private String name;

    @Schema(description = "父栏目ID（可选，传入表示移动栏目）")
    private String parentId;

    @Schema(description = "同级排序值（可选）")
    private Integer sortNum;
}
