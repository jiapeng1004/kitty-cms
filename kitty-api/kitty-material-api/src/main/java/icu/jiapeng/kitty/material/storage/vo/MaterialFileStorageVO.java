package icu.jiapeng.kitty.material.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存储配置展示。接口需 {@code material:storage:manage}，对已通过权限校验的管理员返回 AK/SK 明文便于运维。
 */
@Data
@Schema(description = "文件存储配置")
public class MaterialFileStorageVO {

    @Schema(description = "存储主键（即对外 storageId，唯一标识）")
    private String id;

    @Schema(description = "引擎类型：s3 / disk")
    private String storageType;

    @Schema(description = "桶/挂载点")
    private String bucket;

    @Schema(description = "内网 Endpoint")
    private String internalEndpoint;

    @Schema(description = "外网 Endpoint")
    private String externalEndpoint;

    @Schema(description = "Access Key（明文）")
    private String accessKey;

    @Schema(description = "Secret Key（明文；未配置则为空）")
    private String secretKey;

    @Schema(description = "是否已配置 Secret（与 secretKey 是否非空一致）")
    private Boolean secretConfigured;

    @Schema(description = "是否主存储（上传未传 storageId 时落此存储）")
    private Boolean primaryStorage;
}
