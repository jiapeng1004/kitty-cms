package icu.jiapeng.kitty.material.embedding.adapter;

import icu.jiapeng.kitty.material.embedding.KtEmbeddingRequest;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingDTO;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingPort;
import icu.jiapeng.kitty.material.embedding.MaterialVectorSourceKey;
import lombok.RequiredArgsConstructor;

/**
 * 按 {@link MaterialVectorSourceKey} 路由；当前仅注册 NONE，其它来源可后续扩展 Bean。
 */
@RequiredArgsConstructor
public class RoutingMaterialVectorEmbeddingPort implements KtEmbeddingPort {

    private final NoneMaterialVectorEmbeddingAdapter noneAdapter;

    @Override
    public KtEmbeddingDTO embed(KtEmbeddingRequest request) {
        if (request == null || request.getSourceKey() == null) {
            throw new IllegalArgumentException("request.sourceKey required");
        }
        MaterialVectorSourceKey key = request.getSourceKey();
        if (key.isNone()) {
            return noneAdapter.embed(request);
        }
        throw new UnsupportedOperationException("no vector embedding adapter for source: " + key.code());
    }
}
