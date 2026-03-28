package icu.jiapeng.kitty.material.embedding;

import lombok.Builder;
import lombok.Value;

/**
 * 向量化请求：文本/资源维度由调用方传入；具体模型由 {@link #sourceKey} 选择。
 */
@Value
@Builder
public class KtEmbeddingRequest {

    MaterialVectorSourceKey sourceKey;
    /** 可选：用于审计或异步任务关联 */
    String resourceId;
    /** 待编码文本（标题、摘要、OCR 拼接等） */
    String text;
}
