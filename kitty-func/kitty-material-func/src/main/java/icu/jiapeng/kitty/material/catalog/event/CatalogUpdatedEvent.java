package icu.jiapeng.kitty.material.catalog.event;

import cn.hutool.core.util.StrUtil;

/**
 * 栏目更新成功后的业务事件（重命名、移动、排序等）。
 */
public record CatalogUpdatedEvent(String catalogId) {
    public CatalogUpdatedEvent {
        if (StrUtil.isBlank(catalogId)) {
            throw new IllegalArgumentException("catalogId must not be blank");
        }
    }
}
