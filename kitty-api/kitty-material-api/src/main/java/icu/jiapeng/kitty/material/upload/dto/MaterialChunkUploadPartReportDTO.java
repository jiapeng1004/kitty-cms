package icu.jiapeng.kitty.material.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分片登记（续传态）")
public class MaterialChunkUploadPartReportDTO {

    @Schema(description = "分片序号，从0开始", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chunkIndex;

    @Schema(description = "本分片字节数，须与规划一致", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long byteSize;
}
