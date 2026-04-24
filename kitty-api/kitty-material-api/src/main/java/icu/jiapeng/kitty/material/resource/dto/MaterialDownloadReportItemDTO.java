package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "单条下载行为上报")
public class MaterialDownloadReportItemDTO {

    @Schema(description = "资源 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceId;

    @Schema(description = "用户意图选择的码率/分级 destinationType", requiredMode = Schema.RequiredMode.REQUIRED)
    private String destinationType;

    @Schema(description = "资源标题快照")
    private String resourceTitle;

    @Schema(description = "实际拉取时的分级，批量降级为源码等场景必填")
    private String actualDestinationType;
}
