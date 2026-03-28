package icu.jiapeng.kitty.material.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "模板内字段（表单/绑定展示）")
public class MaterialMetadataFormFieldVO {

    private String fieldId;
    private String fieldCode;
    private String fieldName;
    private String inputType;
    private Integer required;
    private String optionsJson;
    private Integer sortNum;
}
