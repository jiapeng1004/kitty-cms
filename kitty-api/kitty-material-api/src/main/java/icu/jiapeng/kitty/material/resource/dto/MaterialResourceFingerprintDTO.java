package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 资源文件指纹参数。
 */
@Data
@Schema(description = "资源文件指纹参数")
public class MaterialResourceFingerprintDTO {
    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "分段 CRC32 列表（按顺序）")
    private List<Long> chunkCrc32List;
}
