package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源列表查询参数。
 */
@Data
@Schema(description = "资源列表查询参数")
public class MaterialResourceListQueryDTO {


    @Schema(description = "栏目ID")
    private String catalogId;

    @Schema(description = "父资源ID，根目录为 0；不传则不过滤父级")
    private String parentId;

    @Schema(description = "关键词（与 list 一致：全文检索链路）")
    private String keyword;

    @Schema(description = "语义检索文本（与 list 一致）")
    private String semanticText;

    /**
     * 资源标题
     */
    @Schema(description = "title")
    private String title;

    /**
     * 所在栏目树形编码
     */
    @Schema(description = "所在栏目树形编码")
    private String catalogTreeCode;

    /**
     * {@link icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum}
     */
    @Schema(description = "资源类型，见 ResourceTypeEnum。")
    private Integer type;


    @Schema(description = "最小文件大小（字节）。")
    private Long minFileSize;

    @Schema(description = "最大文件大小（字节）。")
    private Long maxFileSize;

    @Schema(description = "文件指纹（大小 + 分段 CRC 归一化串）。")
    private String fingerprint;

    /**
     * 逻辑删/回收站：0 正常、1 已入回收站
     */
    @Schema(description = "回收站资源")
    private Boolean isRecycled;


    @Schema(description = "检索最大条数，默认 20，最大 200")
    private Integer limit;
}
