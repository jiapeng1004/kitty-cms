package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 将资源与存储记录、对象键绑定写入 meta_file。
 */
@Data
@Schema(description = "资源物理文件绑定请求")
public class MaterialMetaFileBindDTO {

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceId;

    @Schema(description = "存储记录ID（file_storage.id）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageId;

    @Schema(description = "对象键（将按驱动规则规范化）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectKey;

    @Schema(description = "展示文件名，可选，默认取资源标题")
    private String name;
}
