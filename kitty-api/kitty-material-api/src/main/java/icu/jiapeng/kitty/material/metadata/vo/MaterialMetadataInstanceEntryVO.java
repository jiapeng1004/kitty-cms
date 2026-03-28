package icu.jiapeng.kitty.material.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目实例字段项")
public class MaterialMetadataInstanceEntryVO {

    private String fieldId;
    private String fieldCode;
    private String fieldName;
    private String fieldValue;
    private Integer version;
    private Integer lastVersion;
    private String templateId;
}
