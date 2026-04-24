package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量下载行为结束上报")
public class MaterialDownloadReportBatchDTO {

    @Schema(description = "各资源实际拉取情况")
    private List<MaterialDownloadReportItemDTO> items;
}
