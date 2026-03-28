package icu.jiapeng.kitty.material.upload.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("kt_chunk_upload_session")
@EqualsAndHashCode(callSuper = true)
public class KtChunkUploadSession extends CommonEntity {

    @TableField("resource_id")
    private String resourceId;

    @TableField("catalog_id")
    private String catalogId;

    @TableField("parent_resource_id")
    private String parentResourceId;

    @TableField("title")
    private String title;

    @TableField("resource_type")
    private Integer resourceType;

    @TableField("precatalog_json")
    private String precatalogJson;

    @TableField("storage_id")
    private String storageId;

    @TableField("object_key")
    private String objectKey;

    @TableField("total_size")
    private Long totalSize;

    @TableField("chunk_size")
    private Long chunkSize;

    @TableField("chunk_count")
    private Integer chunkCount;

    @TableField("status")
    private String status;
}
