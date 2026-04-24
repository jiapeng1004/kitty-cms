package icu.jiapeng.kitty.material.resource.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源衍生文件：封面、雪碧、转码多码率等；SOURCE 不持久化、由主 meta_file 表示。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_resource_derivative")
public class KtResourceDerivative extends CommonEntity {

    @TableField("resource_id")
    private String resourceId;

    @TableField("destination_type")
    private String destinationType;

    @TableField("storage_id")
    private String storageId;

    @TableField("object_key")
    private String objectKey;

    @TableField("external_url")
    private String externalUrl;

    @TableField("file_size")
    private Long fileSize;

    @TableField("meta_json")
    private String metaJson;
}
