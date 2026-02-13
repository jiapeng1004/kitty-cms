package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import icu.jiapeng.kitty.transcoder.func.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/transcode")
public class TranscodeController implements TranscodeApi {

    @Autowired
    private TaskService taskService;

    @PostMapping("/task")
    public String createTask(@RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping("/task/{id}")
    public TaskVO getTask(@PathVariable("id") String id) {
        return taskService.getTask(id);
    }

    @DeleteMapping("/task/{id}")
    public Boolean cancelTask(@PathVariable("id") String id) {
        return taskService.cancelTask(id);
    }

    @GetMapping("/progress/{id}")
    public ProgressVO getProgress(@PathVariable("id") String id) {
        return taskService.getProgress(id);
    }

    @Autowired
    private StrategyService strategyService;

    @PostMapping("/strategy")
    public String createStrategy(@RequestBody CreateStrategyRequest request) {
        return strategyService.createStrategy(request);
    }

    @GetMapping("/strategy")
    public List<StrategyVO> getStrategies() {
        return strategyService.getStrategies();
    }

    @GetMapping("/strategy/{id}")
    public StrategyVO getStrategy(@PathVariable("id") String id) {
        return strategyService.getStrategy(id);
    }

    @PutMapping("/strategy/{id}")
    public Boolean updateStrategy(@PathVariable("id") String id, @RequestBody CreateStrategyRequest request) {
        return strategyService.updateStrategy(id, request);
    }

    @DeleteMapping("/strategy/{id}")
    public Boolean deleteStrategy(@PathVariable("id") String id) {
        return strategyService.deleteStrategy(id);
    }

    @GetMapping("/sse/{id}")
    public SseEmitter getProgressSSE(@PathVariable("id") String id) {
        return taskService.getProgressSSE(id);
    }
}
