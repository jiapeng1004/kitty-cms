package icu.jiapeng.kitty.material.searchsync;

import java.util.List;

/**
 * 检索查询端口：全文与向量检索由 ES 实现；未启用 ES 时由空实现 + 应用层降级。
 */
public interface MaterialSearchQueryPort {

    /**
     * 全文检索，返回资源 ID 列表（按相关度）。
     */
    List<String> searchIdsByFullText(String keyword, String catalogId, String parentId, int size);

    /**
     * kNN 向量检索，返回资源 ID 列表。
     */
    List<String> searchIdsByKnn(float[] queryVector, String catalogId, String parentId, int k);
}
