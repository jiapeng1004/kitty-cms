package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询最新编目")
public class MaterialMetadataLastQueryDTO {

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "模板ID")
    private String templateId;
}
