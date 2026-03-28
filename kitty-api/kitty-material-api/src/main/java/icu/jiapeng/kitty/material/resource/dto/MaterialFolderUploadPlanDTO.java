package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 文件夹上传入口编排参数（相对路径清单）。
 */
@Data
@Schema(description = "文件夹上传入口编排参数")
public class MaterialFolderUploadPlanDTO {
    @Schema(description = "栏目ID")
    private String catalogId;

    @Schema(description = "挂载父资源ID，默认0")
    private String parentId;

    @Schema(description = "相对路径列表，如 a/b/file.mp4")
    private List<String> relativePaths;
}
