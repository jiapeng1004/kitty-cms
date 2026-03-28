package icu.jiapeng.kitty.material.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 栏目权限树节点。
 */
@Data
@Schema(description = "栏目权限树节点")
public class MaterialCatalogPermissionNodeVO {

    @Schema(description = "栏目ID")
    private String id;

    @Schema(description = "父级ID")
    private String parentId;

    @Schema(description = "栏目名称")
    private String name;

    @Schema(description = "是否虚拟根")
    private Boolean virtualRoot;

    @Schema(description = "排序值")
    private Integer sortNum;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "当前用户是否有权限")
    private Boolean allowed;

    @Schema(description = "子节点")
    private List<MaterialCatalogPermissionNodeVO> children;
}
