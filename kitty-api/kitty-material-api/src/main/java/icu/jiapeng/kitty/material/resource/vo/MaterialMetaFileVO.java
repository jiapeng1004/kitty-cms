package icu.jiapeng.kitty.material.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源物理文件记录。
 */
@Data
@Schema(description = "资源物理文件记录 VO")
public class MaterialMetaFileVO {

    @Schema(description = "文件记录ID")
    private String id;

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "展示文件名")
    private String name;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "资源侧相对路径快照")
    private String relaPath;

    @Schema(description = "存储记录ID")
    private String storageId;

    @Schema(description = "对象键")
    private String objectKey;
}
