package icu.jiapeng.kitty.user.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单树节点")
public class MenuTreeVo {
    @Schema(description = "菜单 id")
    private String id;

    @Schema(description = "父级 id")
    private String parentId;

    @Schema(description = "名称")
    private String menuName;

    @Schema(description = "节点类型：DIR=目录，MENU=菜单，LINK=链接")
    private String menuType;

    @Schema(description = "前端唯一 key（用于路由/菜单 key）")
    private String menuKey;

    @Schema(description = "前端路由 path")
    private String path;

    @Schema(description = "前端组件路径（用于动态路由）")
    private String component;

    @Schema(description = "链接类型：INTERNAL/EXTERNAL，仅当 menuType=LINK 时有效")
    private String linkType;

    @Schema(description = "链接地址，仅当 menuType=LINK 时有效")
    private String linkUrl;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "排序（越小越靠前）")
    private Integer sort;

    @Schema(description = "绑定的权限 code（逗号分隔）")
    private String pCodes;

    @Schema(description = "子节点")
    private List<MenuTreeVo> children;
}

