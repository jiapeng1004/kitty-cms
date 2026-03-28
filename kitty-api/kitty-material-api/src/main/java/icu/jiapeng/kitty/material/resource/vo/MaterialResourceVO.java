package icu.jiapeng.kitty.material.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源展示对象。
 */
@Data
@Schema(description = "资源展示对象")
public class MaterialResourceVO {

    @Schema(description = "资源ID")
    private String id;

    @Schema(description = "资源标题")
    private String title;

    @Schema(description = "栏目ID")
    private String catalogId;

    @Schema(description = "父资源ID")
    private String parentId;

    @Schema(description = "资源路径")
    private String path;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "分段CRC32列表（逗号分隔）")
    private String chunkCrc32List;

    @Schema(description = "文件指纹")
    private String fingerprint;

    @Schema(description = "资源类型")
    private Integer type;
}
