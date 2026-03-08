package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyExportService;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import icu.jiapeng.kitty.transcoder.func.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/transcode")
@Tag(name = "转码任务与策略", description = "任务 CRUD、进度、策略管理、SSE 实时推送")
@Slf4j
public class TranscodeController implements TranscodeApi {

    @Resource
    private TaskService taskService;

    @Resource
    private StrategyService strategyService;

    @Resource
    private StrategyExportService strategyExportService;

    @Operation(summary = "创建转码任务")
    @PostMapping("/task")
    @Override
    public String createTask(@RequestBody CreateTaskRequest request) {
        String accessKeyId = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            accessKeyId = (String) attrs.getRequest().getAttribute(TranscodeConstants.ATTR_ACCESS_KEY_ID);
        }
        return taskService.createTask(request, accessKeyId);
    }

    @Operation(summary = "查询任务详情")
    @GetMapping("/task/{id}")
    @Override
    public TaskVO getTask(@PathVariable @Parameter(description = "任务 ID") String id) {
        return taskService.getTask(id);
    }

    @Operation(summary = "分页查询任务列表")
    @GetMapping("/tasks")
    @Override
    public ListTasksResponse listTasks(@ModelAttribute ListTasksRequest request) {
        return taskService.listTasks(request);
    }

    @DeleteMapping("/task/{id}")
    @Override
    public Boolean cancelTask(@PathVariable String id) {
        return taskService.cancelTask(id);
    }

    @DeleteMapping("/task/{id}/record")
    @Override
    public Boolean deleteTask(@PathVariable String id) {
        return taskService.deleteTask(id);
    }

    @Operation(summary = "任务整体重试")
    @PostMapping("/task/{id}/retry")
    @Override
    public Boolean retryTask(@PathVariable String id) {
        return taskService.retryTask(id);
    }

    @Operation(summary = "单步骤重试")
    @PostMapping("/task/{id}/step/{stepId}/retry")
    @Override
    public String retryStep(@PathVariable String id, @PathVariable Integer stepId) throws Exception {
        return taskService.retryStep(id, stepId);
    }

    @Operation(summary = "查询任务进度")
    @GetMapping("/progress/{id}")
    @Override
    public ProgressVO getProgress(@PathVariable @Parameter(description = "任务 ID") String id) {
        return taskService.getProgress(id);
    }


    @PostMapping("/strategy")
    @Override
    public String createStrategy(@RequestBody CreateStrategyRequest request) {
        return strategyService.createStrategy(request);
    }

    @GetMapping("/strategy")
    @Override
    public List<StrategyVO> getStrategies() {
        return strategyService.getStrategies();
    }

    @GetMapping("/strategy/{id}")
    @Override
    public StrategyVO getStrategy(@PathVariable String id) {
        return strategyService.getStrategy(id);
    }

    @PutMapping("/strategy/{id}")
    @Override
    public Boolean updateStrategy(@PathVariable String id, @RequestBody CreateStrategyRequest request) {
        return strategyService.updateStrategy(id, request);
    }

    @DeleteMapping("/strategy/{id}")
    @Override
    public Boolean deleteStrategy(@PathVariable String id) {
        return strategyService.deleteStrategy(id);
    }

    @Operation(summary = "导出策略为 YAML 文件")
    @GetMapping("/strategy/{id}/export")
    @Override
    public String exportStrategy(@PathVariable String id) {
        StrategyVO vo = strategyService.getStrategy(id);
        if (vo == null) throw new IllegalArgumentException("策略不存在");
        return strategyExportService.exportToYaml(vo);
    }

    @Operation(summary = "从 YAML 文件导入策略")
    @PostMapping("/strategy/import")
    @Override
    public String importStrategy(@Valid @RequestBody ImportStrategyRequest request) {
        return strategyExportService.importFromString(request.getContent());
    }

    @Operation(summary = "修改策略ID")
    @PutMapping("/strategy/{id}/id")
    @Override
    public Boolean updateStrategyId(@PathVariable String id, @Valid @RequestBody UpdateStrategyIdRequest request) {
        strategyService.updateStrategyId(id, request.getNewId());
        return true;
    }

    @GetMapping("/sse/{id}")
    @Override
    public Flux<ServerSentEvent<ProgressVO>> getProgressSSE(@PathVariable String id) {
        return taskService.getProgressSSE(id);
    }

    @GetMapping("/progress/stream")
    @Override
    public Flux<ServerSentEvent<ProgressVO>> getProgressStream() {
        return taskService.getProgressStream();
    }

    @Operation(summary = "自省观察员", description = "HTTP 通知回调端点，开发阶段可将 notifications.target 指向本接口，仅记录日志")
    @PostMapping("/introspection/notification")
    @Override
    public void introspectionNotification(@RequestBody TranscodeProgressNotifyVO body) {
        log.info("[Introspection] HTTP notification: {}", body);
    }
}
