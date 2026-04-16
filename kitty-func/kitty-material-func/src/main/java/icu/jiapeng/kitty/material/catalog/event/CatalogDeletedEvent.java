package icu.jiapeng.kitty.material.catalog.event;

import cn.hutool.core.util.StrUtil;

/**
 * 栏目删除成功后的业务事件。
 */
public record CatalogDeletedEvent(String catalogId) {
    public CatalogDeletedEvent {
        if (StrUtil.isBlank(catalogId)) {
            throw new IllegalArgumentException("catalogId must not be blank");
        }
    }
}
