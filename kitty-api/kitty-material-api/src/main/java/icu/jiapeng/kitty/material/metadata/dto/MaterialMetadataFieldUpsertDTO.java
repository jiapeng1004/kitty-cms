package icu.jiapeng.kitty.material.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目字段定义创建/更新")
public class MaterialMetadataFieldUpsertDTO {

    @Schema(description = "字段ID（更新必填）")
    private String id;

    @Schema(description = "字段编码（全局唯一）")
    private String fieldCode;

    @Schema(description = "展示名称")
    private String fieldName;

    @Schema(description = "输入类型：TEXT/NUMBER/SELECT 等")
    private String inputType;

    @Schema(description = "是否必填 1/0")
    private Integer required;

    @Schema(description = "可选值 JSON 数组字符串，如 [\"a\",\"b\"]（SELECT 时使用）")
    private String optionsJson;
}
