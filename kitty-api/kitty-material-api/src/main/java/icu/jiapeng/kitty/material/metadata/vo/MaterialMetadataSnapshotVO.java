package icu.jiapeng.kitty.material.metadata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "某模板下一版编目快照（同一 version）")
public class MaterialMetadataSnapshotVO {

    private String templateId;
    private String templateName;
    private Integer version;
    private List<MaterialMetadataInstanceEntryVO> entries;
}
