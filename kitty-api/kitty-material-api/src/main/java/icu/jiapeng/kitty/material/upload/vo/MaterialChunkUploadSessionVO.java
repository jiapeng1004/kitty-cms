package icu.jiapeng.kitty.material.upload.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分片上传会话（含续传分片列表）")
public class MaterialChunkUploadSessionVO {

    private String id;
    private String resourceId;

    @Schema(description = "MAM 会话快照：栏目")
    private String catalogId;

    @Schema(description = "父资源 ID 快照")
    private String parentResourceId;

    @Schema(description = "标题快照")
    private String title;

    @Schema(description = "资源类型快照")
    private Integer resourceType;

    private String storageId;
    private String objectKey;
    private Long totalSize;
    private Long chunkSize;
    private Integer chunkCount;
    private String status;

    @Schema(description = "已登记分片，按序号升序")
    private List<MaterialChunkUploadPartVO> parts;
}
