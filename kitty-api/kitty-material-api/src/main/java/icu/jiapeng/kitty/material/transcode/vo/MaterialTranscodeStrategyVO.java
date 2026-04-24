package icu.jiapeng.kitty.material.transcode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MaterialTranscodeStrategyVO {

    private String id;
    private String name;
    private String platformCode;
    private String externalStrategyId;
    private String paramsJson;
    private Integer enabled;

    private Integer resourceType;
    private Integer isGlobalDefault;
}
