package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.auth.TokenAuthFilter;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import icu.jiapeng.kitty.transcoder.func.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/transcode")
@Tag(name = "转码任务与策略", description = "任务 CRUD、进度、策略管理、SSE 实时推送")
public class TranscodeController implements TranscodeApi {

    private static final Logger log = LoggerFactory.getLogger(TranscodeController.class);

    @Resource
    private TaskService taskService;

    @Resource
    private StrategyService strategyService;

    @Operation(summary = "创建转码任务")
    @PostMapping("/task")
    public String createTask(@RequestBody CreateTaskRequest request) {
        String accessKeyId = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            accessKeyId = (String) attrs.getRequest().getAttribute(TokenAuthFilter.ATTR_ACCESS_KEY_ID);
        }
        return taskService.createTask(request, accessKeyId);
    }

    @Operation(summary = "查询任务详情")
    @GetMapping("/task/{id}")
    public TaskVO getTask(@PathVariable @Parameter(description = "任务 ID") String id) {
        return taskService.getTask(id);
    }

    @Operation(summary = "分页查询任务列表")
    @GetMapping("/tasks")
    public List<TaskVO> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String taskId) {
        return taskService.listTasks(page, size, taskId);
    }

    @DeleteMapping("/task/{id}")
    public Boolean cancelTask(@PathVariable String id) {
        return taskService.cancelTask(id);
    }

    @DeleteMapping("/task/{id}/record")
    public Boolean deleteTask(@PathVariable String id) {
        return taskService.deleteTask(id);
    }

    @Operation(summary = "查询任务进度")
    @GetMapping("/progress/{id}")
    public ProgressVO getProgress(@PathVariable @Parameter(description = "任务 ID") String id) {
        return taskService.getProgress(id);
    }


    @PostMapping("/strategy")
    public String createStrategy(@RequestBody CreateStrategyRequest request) {
        return strategyService.createStrategy(request);
    }

    @GetMapping("/strategy")
    public List<StrategyVO> getStrategies() {
        return strategyService.getStrategies();
    }

    @GetMapping("/strategy/{id}")
    public StrategyVO getStrategy(@PathVariable String id) {
        return strategyService.getStrategy(id);
    }

    @PutMapping("/strategy/{id}")
    public Boolean updateStrategy(@PathVariable String id, @RequestBody CreateStrategyRequest request) {
        return strategyService.updateStrategy(id, request);
    }

    @DeleteMapping("/strategy/{id}")
    public Boolean deleteStrategy(@PathVariable String id) {
        return strategyService.deleteStrategy(id);
    }

    @GetMapping("/sse/{id}")
    public SseEmitter getProgressSSE(@PathVariable String id) {
        return taskService.getProgressSSE(id);
    }

    @GetMapping("/progress/stream")
    public SseEmitter getProgressStream() {
        return taskService.getProgressStream();
    }

    @Operation(summary = "自省观察员", description = "HTTP 通知回调端点，开发阶段可将 notifications.target 指向本接口，仅记录日志")
    @PostMapping("/introspection/notification")
    public void introspectionNotification(@RequestBody TranscodeProgressNotifyVO body) {
        log.info("[Introspection] HTTP notification: {}", body);
    }
}
