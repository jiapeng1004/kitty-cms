package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务列表查询参数
 */
@Data
@Schema(description = "任务列表查询参数")
public class ListTasksRequest {

    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer size = 20;

    @Schema(description = "任务 ID 模糊检索")
    private String taskId;

    @Schema(description = "文件名模糊检索（inputPath）")
    private String filename;

    @Schema(description = "创建时间起（毫秒时间戳）")
    private Long timeFrom;

    @Schema(description = "创建时间止（毫秒时间戳）")
    private Long timeTo;

    @Schema(description = "策略 ID 过滤（root_id）")
    private Long strategyId;

    @Schema(description = "任务状态过滤：PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED")
    private String status;

    @Schema(description = "任务类型过滤：SCHEDULED_TRANSCODE, MAGIC_EXTRACT_FRAMES, MAGIC_IMAGE_CONVERT, MAGIC_SYNC_TRANSCODE")
    private String taskType;

    @Schema(description = "排序字段：createdAt, completedAt", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "排序方向：asc, desc", example = "desc")
    private String sortOrder = "desc";
}
