package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目历史版本查询（version<=maxVersion）")
public class MaterialMetadataHistoryQueryDTO {

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "版本上界（含）")
    private Integer maxVersion;
}
