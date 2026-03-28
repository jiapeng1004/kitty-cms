package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件夹创建参数。
 */
@Data
@Schema(description = "文件夹创建参数")
public class MaterialFolderCreateDTO {
    @Schema(description = "文件夹名称")
    private String title;

    @Schema(description = "栏目ID")
    private String catalogId;

    @Schema(description = "父资源ID，默认0")
    private String parentId;
}
