package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页任务列表响应，含分页信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页任务列表响应")
public class ListTasksResponse {

    @Schema(description = "当前页任务列表")
    private List<TaskVO> list;

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "当前页码，从 1 开始")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
