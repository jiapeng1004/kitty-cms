package icu.jiapeng.kitty.material.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "预编目载荷（随上传会话一并保存，合并成功后写入元数据实例）")
public class MaterialPrecatalogPayloadDTO {

    @Schema(description = "编目模板ID")
    private String templateId;

    @Schema(description = "字段编码 → 值")
    private Map<String, String> fieldValues;
}
