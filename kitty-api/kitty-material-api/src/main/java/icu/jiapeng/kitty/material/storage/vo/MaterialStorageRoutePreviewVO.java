package icu.jiapeng.kitty.material.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存储路由预览结果。
 */
@Data
@Schema(description = "存储路由预览结果")
public class MaterialStorageRoutePreviewVO {
    @Schema(description = "存储ID")
    private String storageId;

    @Schema(description = "引擎类型")
    private Integer engineType;

    @Schema(description = "驱动名称")
    private String driverName;

    @Schema(description = "对象键")
    private String objectKey;

    @Schema(description = "路由目标")
    private String routeTarget;
}
