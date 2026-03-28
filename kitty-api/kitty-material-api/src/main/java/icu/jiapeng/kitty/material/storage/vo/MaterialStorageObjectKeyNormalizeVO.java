package icu.jiapeng.kitty.material.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 对象键规范化结果。
 */
@Data
@Schema(description = "对象键规范化结果")
public class MaterialStorageObjectKeyNormalizeVO {
    @Schema(description = "原始对象键")
    private String originalKey;

    @Schema(description = "规范化对象键")
    private String normalizedKey;

    @Schema(description = "是否有效")
    private Boolean valid;

    @Schema(description = "驱动名称")
    private String driverName;
}
