package icu.jiapeng.kitty.material.catalog.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * 栏目权限
 *
 * @author jiapeng
 * @since 2026/3/28
 */
@Getter
@AllArgsConstructor
public enum CatalogPermission {
    /**
     * 栏目树结构及其下属资源查看权限
     */
    VIEW("view", "栏目树结构及其下属资源查看权限"),
    /**
     * 栏目修改权限
     */
    EDIT("edit", "栏目修改权限"),
    /**
     * 栏目删除权限
     */
    DELETE("delete", "栏目删除权限"),
    /**
     * 栏目新增权限
     */
    ADD("add", "栏目新增权限"),
    ;

    private final String permissionCode;

    private final String permissionDesc;
}
