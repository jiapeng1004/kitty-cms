package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "按模板保存编目实例（字段编码 -> 值）")
public class MaterialMetadataSaveDTO {

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "字段值，key 为 field_code")
    private Map<String, String> fieldValues;
}
