package icu.jiapeng.kitty.material.storage.redis;

/**
 * 素材存储模块 Redis 发布订阅频道名（与业务键一致集中定义，避免魔法字符串）。
 */
public final class MaterialStorageRedisPubSubChannels {
    private MaterialStorageRedisPubSubChannels() {
    }

    /**
     * kt_file_storage 记录更新或删除时发布存储 id，各节点驱逐该 id 下缓存的 S3 客户端。
     */
    public static final String S3_STORAGE_CLIENT_INVALIDATE = "material:storage:s3:client:invalidate";
}
