package icu.jiapeng.kitty.material.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新存储配置。
 */
@Data
@Schema(description = "文件存储配置保存")
public class MaterialFileStorageUpsertDTO {

    @Schema(description = "存储主键（对外 storageId）。更新时必填；新建时可选——有值则作为 id 落库，留空则由服务端 SecureRandom 生成")
    private String id;

    @NotBlank
    @Schema(description = "引擎：s3（对象存储）或 disk（磁盘）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageType;

    @Schema(description = "桶名 / 磁盘挂载路径（disk 时多为本地根路径）")
    private String bucket;

    @Schema(description = "内网 Endpoint（S3 兼容时填服务地址，如 MinIO / OSS endpoint）")
    private String internalEndpoint;

    @Schema(description = "外网访问地址（可选）")
    private String externalEndpoint;

    @Schema(description = "Access Key")
    private String accessKey;

    /**
     * 新建必填；更新时若为空表示不修改原 Secret。
     */
    @Schema(description = "Secret Key（更新时留空表示不修改）")
    private String secretKey;

    @Schema(description = "是否主存储；全局仅能有一个主存储，未指定分片上传 storageId 时使用主存储")
    private Boolean primaryStorage;
}
