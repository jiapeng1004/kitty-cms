package icu.jiapeng.kitty.material.searchsync;

/**
 * 检索同步 Kafka topic 名（默认与 {@code material.search.sync.kafka.topic} 一致，可覆盖）。
 */
public final class MaterialSearchSyncChannels {

    /** 资源/编目变更后触发检索索引同步（ES 等） */
    public static final String SEARCH_SYNC = "material.search.sync";

    private MaterialSearchSyncChannels() {
    }
}
