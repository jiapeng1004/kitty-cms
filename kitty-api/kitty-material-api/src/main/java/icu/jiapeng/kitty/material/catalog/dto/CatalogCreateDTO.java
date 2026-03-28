package icu.jiapeng.kitty.material.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 栏目节点（领域对象，禁止 ORM 注解上浮）。
 */
@Data
@Schema(description = "栏目创建DTO")
public class CatalogCreateDTO {

    /**
     * 父节点ID，无父级固定为 0。
     */
    @Schema(description = "父节点ID，无父级固定为 0")
    private String parentId;

    /**
     * 名称。
     */
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    /**
     * 同级排序值。
     */
    @Schema(description = "同级排序值")
    private Integer sortNum;
}
