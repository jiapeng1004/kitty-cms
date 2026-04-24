package icu.jiapeng.kitty.material.transcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MaterialTranscodeStrategyUpsertDTO {

    @Schema(description = "策略ID，更新必填")
    private String id;

    private String name;

    @Schema(description = "如 kitty_transcoder_grpc")
    private String platformCode;

    @Schema(description = "转码服务策略ID（数字字符串）")
    private String externalStrategyId;

    private String paramsJson;

    private Integer enabled;

    @Schema(description = "资源类型 ResourceTypeEnum，与全局默认、栏目解析一致；设全局默认时必填")
    private Integer resourceType;

    @Schema(description = "1=该资源类型下全局默认（同类至多一条）")
    private Integer isGlobalDefault;
}
