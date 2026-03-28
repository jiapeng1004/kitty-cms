package icu.jiapeng.kitty.material.searchsync;

/**
 * 检索索引侧同步语义：全量文档重建与编目字段增量（便于 ES partial update）。
 */
public enum MaterialSearchSyncMessageType {

    /** 资源主数据及可聚合字段需整文档重算（新建、更新、绑定文件、指纹等）。 */
    FULL_DOCUMENT,

    /** 仅编目实例变更：可映射为 ES partial update（templateId + 字段编码集合）。 */
    METADATA_FIELD_PATCH
}
