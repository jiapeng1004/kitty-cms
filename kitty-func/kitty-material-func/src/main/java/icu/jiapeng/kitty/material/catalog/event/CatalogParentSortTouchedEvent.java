package icu.jiapeng.kitty.material.catalog.event;

import cn.hutool.core.util.StrUtil;

/**
 * 某父栏目下子栏目 sort 已变更（创建、移动、排序后），在事务提交后可能触发归一化检查。
 */
public record CatalogParentSortTouchedEvent(String parentId) {
    public CatalogParentSortTouchedEvent {
        if (StrUtil.isBlank(parentId)) {
            throw new IllegalArgumentException("parentId must not be blank");
        }
    }
}
