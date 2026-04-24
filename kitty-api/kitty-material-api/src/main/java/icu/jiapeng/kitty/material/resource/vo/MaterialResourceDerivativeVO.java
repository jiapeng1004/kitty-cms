package icu.jiapeng.kitty.material.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "资源某一分级/衍生产物的可展示与下载元数据")
@Data
public class MaterialResourceDerivativeVO {

    @Schema(description = "分级标识，如 SOURCE、720P、COVER、SPRITE")
    private String destinationType;

    @Schema(description = "是否可下载（有 URL 或同源存储键）")
    private Boolean available;

    @Schema(description = "可直接给浏览器或 <img> 的地址（或同源绝对 URL）")
    private String accessUrl;

    @Schema(description = "字节大小，未知时 null")
    private Long fileSize;
}
