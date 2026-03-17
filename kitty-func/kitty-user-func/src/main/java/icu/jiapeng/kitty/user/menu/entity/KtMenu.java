/*
 * 菜单实体：后台侧边栏 / 顶部菜单树
 */
package icu.jiapeng.kitty.user.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("kt_menu")
public class KtMenu extends CommonEntity {

    @TableField("parent_id")
    private String parentId;

    @TableField("menu_name")
    private String menuName;

    /**
     * 节点类型：DIR=目录，MENU=菜单，LINK=链接
     */
    @TableField("menu_type")
    private String menuType;

    @TableField("menu_key")
    private String menuKey;

    @TableField("path")
    private String path;

    @TableField("icon")
    private String icon;

    /**
     * 前端组件路径（用于动态路由，如 admin/views/...）
     */
    @TableField("component")
    private String component;

    /**
     * 链接类型：INTERNAL=同域内链，EXTERNAL=http外链
     */
    @TableField("link_type")
    private String linkType;

    /**
     * 链接地址：当 menuType=LINK 时使用
     */
    @TableField("link_url")
    private String linkUrl;

    @TableField("sort")
    private Integer sort;

    /**
     * 绑定的权限 code（逗号分隔多个），包含任一即可可见
     */
    @TableField("p_codes")
    private String pCodes;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;
}

