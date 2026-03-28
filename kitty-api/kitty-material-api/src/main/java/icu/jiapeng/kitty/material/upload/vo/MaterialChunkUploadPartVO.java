package icu.jiapeng.kitty.material.upload.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "已登记分片")
public class MaterialChunkUploadPartVO {

    private Integer chunkIndex;
    private Long byteSize;
}
