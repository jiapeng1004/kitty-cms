package icu.jiapeng.kitty.material.catalog.vo;

import cn.hutool.core.lang.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 栏目树节点。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "栏目树节点")
public class CatalogNodeVO extends TreeNode<String> {

    @Schema(description = "栏目ID，个人根示例: pri_1001")
    private String id;

    @Schema(description = "父节点ID，无父级固定为0")
    private String parentId;

    @Schema(description = "栏目名称")
    private String name;

    @Schema(description = "是否虚拟根节点")
    private Boolean virtualRoot;

    @Schema(description = "排序值")
    private Integer sortNum;

    @Schema(description = "子节点")
    private List<CatalogNodeVO> children;

    @Schema(description = "权限合集")
    private List<String> permissionCodes;

    public TreeNode<String> setId(String id) {
        this.id = id;
        return this;
    }

    public TreeNode<String> setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    @Override
    public TreeNode<String> setName(CharSequence charSequence) {
        if (charSequence == null) {
            name = null;
            return this;
        }
        name = charSequence.toString();
        return this;
    }

    @Override
    public Comparable<?> getWeight() {
        return sortNum;
    }

    @Override
    public TreeNode<String> setWeight(Comparable<?> comparable) {
        sortNum = (Integer) comparable;
        return this;
    }
}
