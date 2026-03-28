package icu.jiapeng.kitty.material.catalog.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/28
 */
@Getter
@AllArgsConstructor
public enum CatalogType {

    /**
     * 个人栏目根节点
     */
    PRIVATE("pri", "个人栏目"),
    ;

    private final String catalogId;

    private final String name;

}
