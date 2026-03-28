package icu.jiapeng.kitty.material.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建分片上传会话。resourceId 与（catalogId+标题+类型+分片CRC）二选一：无 resourceId 时走 MAM 建档，服务端创建资源并写入指纹；有 resourceId 时为兼容旧流程（须资源上已保存指纹）。")
public class MaterialChunkUploadSessionCreateDTO {

    @Schema(description = "已有资源 ID；与 MAM 字段二选一")
    private String resourceId;

    @Schema(description = "MAM：栏目 ID（无 resourceId 时必填）")
    private String catalogId;

    @Schema(description = "MAM：父资源（文件夹）ID，可空，空则根 0")
    private String parentId;

    @Schema(description = "MAM：资源标题（无 resourceId 时必填）")
    private String title;

    @Schema(description = "MAM：资源类型，见 ResourceTypeEnum（无 resourceId 时必填，勿传文件夹）")
    private Integer type;

    @Schema(description = "MAM：各分片 CRC32 无符号值，顺序须与分片规划一致（totalSize=0 时不传或空列表；无 resourceId 时按规划校验条数）")
    private List<Long> chunkCrc32List;

    @Schema(description = "可选：预编目，合并成功后写入元数据")
    private MaterialPrecatalogPayloadDTO precatalog;

    @Schema(description = "存储ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageId;

    @Schema(description = "对象键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectKey;

    @Schema(description = "文件总字节数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long totalSize;

    @Schema(description = "分片大小（末片可更短）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chunkSize;
}
