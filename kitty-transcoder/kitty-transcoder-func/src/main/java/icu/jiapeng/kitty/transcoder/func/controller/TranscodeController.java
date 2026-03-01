package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.auth.TokenAuthFilter;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import icu.jiapeng.kitty.transcoder.func.task.TaskService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/transcode")
public class TranscodeController implements TranscodeApi {

    @Resource
    private TaskService taskService;

    @Resource
    private StrategyService strategyService;

    @PostMapping("/task")
    public String createTask(@RequestBody CreateTaskRequest request) {
        String accessKeyId = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            accessKeyId = (String) attrs.getRequest().getAttribute(TokenAuthFilter.ATTR_ACCESS_KEY_ID);
        }
        return taskService.createTask(request, accessKeyId);
    }

    @GetMapping("/task/{id}")
    public TaskVO getTask(@PathVariable String id) {
        return taskService.getTask(id);
    }

    @GetMapping("/tasks")
    public List<TaskVO> listTasks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return taskService.listTasks(page, size);
    }

    @DeleteMapping("/task/{id}")
    public Boolean cancelTask(@PathVariable String id) {
        return taskService.cancelTask(id);
    }

    @GetMapping("/progress/{id}")
    public ProgressVO getProgress(@PathVariable String id) {
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
}
