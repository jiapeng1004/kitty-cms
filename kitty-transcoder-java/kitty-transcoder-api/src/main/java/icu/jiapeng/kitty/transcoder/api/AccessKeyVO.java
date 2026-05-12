package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessKeyVO {

    @Schema(description = "Access Key ID")
    private String accessKeyId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间", example = "2025-03-01T12:00:00")
    private String createdAt;
}
