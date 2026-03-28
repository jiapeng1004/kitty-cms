package icu.jiapeng.kitty.material.embedding;

/**
 * 向量化端口：不同 {@link MaterialVectorSourceKey} 可由不同适配器实现（HTTP 远程、本地 ONNX、NONE 占位等）。
 */
@FunctionalInterface
public interface KtEmbeddingPort {

    /**
     * 生成向量；来源为 NONE 时应返回 {@link KtEmbeddingDTO#semanticEmpty()} 为 true 的安全占位结果。
     */
    KtEmbeddingDTO embed(KtEmbeddingRequest request);
}
