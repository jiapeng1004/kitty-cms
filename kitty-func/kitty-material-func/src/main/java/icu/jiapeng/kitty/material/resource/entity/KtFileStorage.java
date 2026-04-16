package icu.jiapeng.kitty.material.resource.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 存储记录表实体
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_file_storage")
public class KtFileStorage extends CommonEntity {
    /**
     * 存储引擎类型
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * 桶/容器/存储挂载点
     */
    @TableField("bucket")
    private String bucket;

    /**
     * 内网地址
     */
    @TableField("internal_endpoint")
    private String internalEndpoint;

    /**
     * 外网地址
     */
    @TableField("external_endpoint")
    private String externalEndpoint;

    /**
     * 访问key
     */
    @TableField("access_key")
    private String accessKey;

    /**
     * 访问secret
     */
    @TableField("secret_key")
    private String secretKey;

    /**
     * 是否主存储（全局至多一条为 true；上传未指定 storageId 时使用）
     */
    @TableField("primary_flag")
    private Boolean primaryFlag;
}
