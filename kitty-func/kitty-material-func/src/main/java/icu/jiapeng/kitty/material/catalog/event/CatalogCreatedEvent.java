package icu.jiapeng.kitty.material.catalog.event;

import cn.hutool.core.util.StrUtil;

/**
 * 栏目创建成功后的业务事件。
 */
public record CatalogCreatedEvent(String catalogId) {
    public CatalogCreatedEvent {
        if (StrUtil.isBlank(catalogId)) {
            throw new IllegalArgumentException("catalogId must not be blank");
        }
    }
}
