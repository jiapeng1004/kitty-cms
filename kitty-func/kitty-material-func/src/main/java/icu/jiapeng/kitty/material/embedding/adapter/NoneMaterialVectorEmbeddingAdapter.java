package icu.jiapeng.kitty.material.embedding.adapter;

import icu.jiapeng.kitty.material.embedding.KtEmbeddingRequest;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingDTO;
import icu.jiapeng.kitty.material.embedding.KtEmbeddingPort;
import icu.jiapeng.kitty.material.embedding.MaterialVectorSourceKey;
import icu.jiapeng.kitty.material.search.MaterialSearchConstants;

/**
 * NONE：不调用外部模型，返回语义空向量，保证编排链路不因 NPE/非法维度中断。
 */
public class NoneMaterialVectorEmbeddingAdapter implements KtEmbeddingPort {

    @Override
    public KtEmbeddingDTO embed(KtEmbeddingRequest request) {
        MaterialVectorSourceKey key = request.getSourceKey();
        if (key == null || !key.isNone()) {
            throw new IllegalArgumentException("NoneMaterialVectorEmbeddingAdapter only supports sourceKey=none, got=" + key);
        }
        int dim = Math.max(0, MaterialSearchConstants.VECTOR_DIMS);
        boolean zeroFill = MaterialSearchConstants.NONE_VECTOR_ZERO_FILL;
        float[] values;
        if (zeroFill) {
            values = new float[dim];
        } else {
            values = new float[0];
        }
        return new KtEmbeddingDTO(MaterialVectorSourceKey.none(), values, dim, true);
    }
}
