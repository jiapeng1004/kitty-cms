package icu.jiapeng.kitty.material.searchsync;

import java.util.Map;

/**
 * 媒资检索索引写入端口（默认 Elasticsearch，可替换为其它实现）。
 */
public interface MaterialSearchIndexPort {

    /**
     * 按资源 ID 整文档覆盖写入（含主数据与聚合编目）。
     *
     * @param resourceId 文档 _id，与 {@code document} 内 resourceId 一致
     * @param document     可序列化为 JSON 的扁平 / 嵌套结构
     */
    void indexOrReplace(String resourceId, Map<String, Object> document);

    /**
     * 部分更新（典型为编目字段 patch）；若索引中尚无该文档，调用方应回退为 {@link #indexOrReplace}。
     */
    void patchDocument(String resourceId, Map<String, Object> partialDocument);

    /**
     * 从检索索引中移除资源文档（如入回收站、彻底删除后）。
     */
    void removeDocument(String resourceId);
}
