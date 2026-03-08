package icu.jiapeng.kitty.transcoder.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "转码任务管理", description = "转码任务的创建、查询、取消等操作")
@HttpExchange("${kitty-transcode-api}")
public interface TranscodeApi {

    @Operation(summary = "创建转码任务", description = "创建一个新的转码任务并返回任务ID")
    @PostExchange("/api/transcode/task")
    String createTask(CreateTaskRequest request);

    @Operation(summary = "查询转码任务", description = "根据任务ID查询转码任务详情")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetExchange("/api/transcode/task/{id}")
    TaskVO getTask(@PathVariable String id);

    @Operation(summary = "分页查询任务列表")
    @GetExchange("/api/transcode/tasks")
    List<TaskVO> listTasks(ListTasksRequest request);

    @Operation(summary = "取消转码任务", description = "取消指定的转码任务")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @DeleteExchange("/api/transcode/task/{id}")
    Boolean cancelTask(@PathVariable String id);

    @Operation(summary = "查询转码策略列表", description = "查询所有可用的转码策略")
    @GetExchange("/api/transcode/strategy")
    List<StrategyVO> getStrategies();

    @Operation(summary = "创建转码策略", description = "创建一个新的转码策略")
    @PostExchange("/api/transcode/strategy")
    String createStrategy(CreateStrategyRequest request);

    @DeleteMapping("/task/{id}/record")
    Boolean deleteTask(@PathVariable String id);

    @Operation(summary = "查询转码进度", description = "根据任务ID查询转码进度")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetExchange("/api/transcode/progress/{id}")
    ProgressVO getProgress(@PathVariable String id);

    @GetMapping("/strategy/{id}")
    StrategyVO getStrategy(@PathVariable String id);

    @PutMapping("/strategy/{id}")
    Boolean updateStrategy(@PathVariable String id, @RequestBody CreateStrategyRequest request);

    @DeleteMapping("/strategy/{id}")
    Boolean deleteStrategy(@PathVariable String id);

    @Operation(summary = "导出策略为 YAML 文件")
    @GetMapping("/strategy/{id}/export")
    String exportStrategy(@PathVariable String id);

    @Operation(summary = "从 YAML 文件导入策略")
    @PostMapping("/strategy/import")
    String importStrategy(@Valid @RequestBody ImportStrategyRequest request);

    @Operation(summary = "修改策略ID")
    @PutMapping("/strategy/{id}/id")
    Boolean updateStrategyId(@PathVariable String id, @Valid @RequestBody UpdateStrategyIdRequest request);

    @Operation(summary = "获取SSE实时进度", description = "通过SSE获取转码任务的实时进度")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetExchange("/api/transcode/sse/{id}")
    Flux<ServerSentEvent<ProgressVO>> getProgressSSE(@PathVariable String id);

    @Operation(summary = "全任务进度SSE流", description = "任意任务进度更新时广播的SSE流")
    @GetExchange("/api/transcode/progress/stream")
    Flux<ServerSentEvent<ProgressVO>> getProgressStream();

    @Operation(summary = "自省观察员", description = "HTTP 通知回调端点，开发阶段可将 notifications.target 指向本接口，仅记录日志")
    @PostMapping("/introspection/notification")
    void introspectionNotification(@RequestBody TranscodeProgressNotifyVO body);
}