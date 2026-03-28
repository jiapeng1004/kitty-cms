package icu.jiapeng.kitty.material.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存储路由预览参数。
 */
@Data
@Schema(description = "存储路由预览参数")
public class MaterialStorageRoutePreviewDTO {
    @Schema(description = "存储ID")
    private String storageId;

    @Schema(description = "对象键")
    private String objectKey;
}
