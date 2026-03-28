package icu.jiapeng.kitty.material.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编目模板 VO")
public class MaterialMetadataTemplateVO {

    private String id;
    private String name;
    private String catalogId;
    private Integer resourceType;
    private Integer enabled;
}
