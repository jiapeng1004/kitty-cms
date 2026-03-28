package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "模板字段绑定（全量替换）")
public class MaterialMetadataTemplateBindFieldsDTO {

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "字段与排序")
    private List<MaterialMetadataFieldBindItemDTO> bindings;
}
