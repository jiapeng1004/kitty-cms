package icu.jiapeng.kitty.material.resource.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源彻底删除后的归档行（EAV/JSON 快照存 MySQL {@code json} 列）。
 */
@TableName("kt_resource_graveyard")
@Data
@EqualsAndHashCode(callSuper = true)
public class KtResourceGraveyard extends CommonEntity {

    @TableField("archive_type")
    private String archiveType;

    @TableField("original_id")
    private String originalId;

    /**
     * 与表列名 {@code json} 对应，持久化为 JSON 或字符串。
     */
    @TableField("json")
    private String json;
}
