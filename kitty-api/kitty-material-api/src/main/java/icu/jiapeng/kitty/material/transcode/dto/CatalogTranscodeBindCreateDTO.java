package icu.jiapeng.kitty.material.transcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CatalogTranscodeBindCreateDTO {

    private String catalogId;
    private String strategyId;
    private Integer resourceType;
    private Integer sortNum;
}
