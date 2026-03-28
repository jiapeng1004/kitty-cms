package icu.jiapeng.kitty.material.embedding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源向量持久化实体（多来源并存）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_resource_embedding")
public class KtResourceEmbedding extends CommonEntity {

    @TableField("resource_id")
    private String resourceId;

    @TableField("source_type")
    private String sourceType;

    @TableField("vector_json")
    private String vectorJson;
}
