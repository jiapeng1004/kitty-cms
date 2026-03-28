package icu.jiapeng.kitty.material.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存储连通性检查结果。
 */
@Data
@Schema(description = "存储连通性检查结果")
public class MaterialStorageConnectivityVO {
    @Schema(description = "存储ID")
    private String storageId;

    @Schema(description = "引擎类型")
    private Integer engineType;

    @Schema(description = "驱动名称")
    private String driverName;

    @Schema(description = "是否可达")
    private Boolean reachable;

    @Schema(description = "连通性详情")
    private String detail;
}
