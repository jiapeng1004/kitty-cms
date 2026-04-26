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

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "分段CRC32列表（逗号分隔）")
    private String chunkCrc32List;

    @Schema(description = "文件指纹")
    private String fingerprint;

    @Schema(description = "资源类型")
    private Integer type;

    /**
     * 浏览器内预览用地址（走 material 预览接口，带权限；非对象存储裸 URL）。
     * 接入转码后可改为指向转码产物；当前实现为 302 至原文件。
     */
    @Schema(description = "预览 URL（相对路径，需拼 API 网关；img/video 等应使用本字段）")
    private String previewUrl;

    /**
     * 对象存储上的原文件直链（下载/转码入参/核对；列表中一般不用于直接展示）。
     */
    @Schema(description = "源文件直链（kt_meta_file + 存储 endpoint/bucket/key）")
    private String srcUrl;

    /**
     * 列表/卡片封面。图片默认与 {@link #srcUrl} 相同（对象存储直链，可直接给前端 img 使用，无需再走预览接口）。
     */
    @Schema(description = "封面 URL（图片默认同 srcUrl，可直接作为 img src）")
    private String coverUrl;

    /**
     * 视频关键帧封面（走 /keyframe；抽帧或转码产出写入存储后再赋值）
     */
    @Schema(description = "视频关键帧 URL（相对路径；未产出时为 null）")
    private String keyframeUrl;

    @Schema(description = "评分")
    private Double score;
}
