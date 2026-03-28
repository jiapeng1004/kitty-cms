package icu.jiapeng.kitty.material.embedding;


/**
 * @param dimension     -- GETTER --
 *                      与 ES dense_vector / 下游模型对齐的维度；语义空向量时仍返回配置的标称维度（如占位全 0）。
 * @param semanticEmpty -- GETTER --
 *                      <p>
 *                      表示未调用真实模型（NONE 或降级），检索侧应跳过向量相似度或仅走关键词。
 */
public record KtEmbeddingDTO(MaterialVectorSourceKey sourceKey, float[] values, int dimension,
                             boolean semanticEmpty) {

    public KtEmbeddingDTO(MaterialVectorSourceKey sourceKey, float[] values, int dimension, boolean semanticEmpty) {
        this.sourceKey = sourceKey;
        this.values = values == null ? new float[0] : values;
        this.dimension = dimension;
        this.semanticEmpty = semanticEmpty;
    }
}
