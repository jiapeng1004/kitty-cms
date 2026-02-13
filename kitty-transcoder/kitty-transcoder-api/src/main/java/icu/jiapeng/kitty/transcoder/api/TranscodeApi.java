package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "转码任务管理", description = "转码任务的创建、查询、取消等操作")
@HttpExchange("${kitty-transcode-api}")
public interface TranscodeApi {

    @Operation(summary = "创建转码任务", description = "创建一个新的转码任务并返回任务ID")
    @PostExchange("/api/transcode/task")
    String createTask(CreateTaskRequest request);

    @Operation(summary = "查询转码任务", description = "根据任务ID查询转码任务详情")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetExchange("/api/transcode/task/{id}")
    TaskVO getTask(String id);

    @Operation(summary = "取消转码任务", description = "取消指定的转码任务")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @DeleteExchange("/api/transcode/task/{id}")
    Boolean cancelTask(String id);

    @Operation(summary = "查询转码策略列表", description = "查询所有可用的转码策略")
    @GetExchange("/api/transcode/strategy")
    java.util.List<StrategyVO> getStrategies();

    @Operation(summary = "创建转码策略", description = "创建一个新的转码策略")
    @PostExchange("/api/transcode/strategy")
    String createStrategy(CreateStrategyRequest request);

    @Operation(summary = "查询转码进度", description = "根据任务ID查询转码进度")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetExchange("/api/transcode/progress/{id}")
    ProgressVO getProgress(String id);

    @Operation(summary = "获取SSE实时进度", description = "通过SSE获取转码任务的实时进度")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetExchange("/api/transcode/sse/{id}")
    SseEmitter getProgressSSE(String id);
}