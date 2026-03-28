package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "模板内字段排序项")
public class MaterialMetadataFieldBindItemDTO {

    @Schema(description = "字段ID")
    private String fieldId;

    @Schema(description = "排序，越小越靠前")
    private Integer sortNum;
}
