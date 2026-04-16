package icu.jiapeng.kitty.material.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存储实例编码可选项（与后端 FileStorageInstanceCodeEnum 一致）。
 */
@Data
@Schema(description = "存储实例编码选项")
public class MaterialStorageInstanceOptionVO {

    @Schema(description = "引擎类型：s3 / disk")
    private String storageType;

    @Schema(description = "建议用作存储主键的预设 id（仅快捷输入，非强制枚举）")
    private String code;

    @Schema(description = "展示名称")
    private String label;
}
