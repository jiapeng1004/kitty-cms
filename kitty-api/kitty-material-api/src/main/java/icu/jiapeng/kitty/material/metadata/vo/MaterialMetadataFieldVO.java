package icu.jiapeng.kitty.material.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目字段 VO")
public class MaterialMetadataFieldVO {

    private String id;
    private String fieldCode;
    private String fieldName;
    private String inputType;
    private Integer required;
    private String optionsJson;
}
