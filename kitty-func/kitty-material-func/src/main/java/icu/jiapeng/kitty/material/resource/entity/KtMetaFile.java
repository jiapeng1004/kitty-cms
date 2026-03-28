package icu.jiapeng.kitty.material.resource.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件JDBC 实体
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_meta_file")
public class KtMetaFile extends CommonEntity {

    /**
     * 资源 ID
     */
    @TableField("resource_id")
    private String resourceId;

    /**
     * 文件名
     */
    @TableField("name")
    private String name;

    /**
     * 文件大小(字节)
     */
    @TableField("size")
    private Long size;

    /**
     * 存储器 ID
     */
    @TableField("storage_id")
    private String storageId;

    /**
     * 对象键
     */
    @TableField("object_key")
    private String objectKey;
}
