package icu.jiapeng.kitty.material.search;

/**
 * 素材检索与 ES mapping 对齐的<strong>代码约定</strong>（非外部配置类；改索引/字段名时改此处与 ES 集群 mapping 一并调整）。
 */
public final class MaterialSearchConstants {

    private MaterialSearchConstants() {
    }

    /** 默认索引名（与 {@code ensureIndex} 创建逻辑一致） */
    public static final String INDEX_NAME = "material_resource";

    /** dense_vector 字段名 */
    public static final String VECTOR_FIELD_NAME = "text_embedding";

    /** 向量来源标记（keyword） */
    public static final String EMBEDDING_SOURCE_FIELD_NAME = "embedding_source";

    /** dense_vector 维度，须与向量化输出一致 */
    public static final int VECTOR_DIMS = 384;

    /**
     * NONE 占位向量化：为 true 时返回长度为 {@link #VECTOR_DIMS} 的全 0 向量；为 false 时 values 可为空数组，仅标称维度有效。
     */
    public static final boolean NONE_VECTOR_ZERO_FILL = true;

    /** dense_vector similarity 原始字符串：cosine / dot_product / l2_norm / max_inner_product */
    public static final String VECTOR_SIMILARITY = "cosine";
}
