package icu.jiapeng.kitty.material.task.api;

import icu.jiapeng.kitty.material.task.dto.MaterialTranscodeEnqueueDTO;
import icu.jiapeng.kitty.material.task.vo.MaterialResourceTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Material-资源任务")
@FeignClient(name = "kitty-mam", contextId = "materialResourceTask")
public interface MaterialResourceTaskApi {

    @Operation(summary = "按资源查询任务列表（未逻辑删除）")
    @GetMapping("/api/material/task/list")
    List<MaterialResourceTaskVO> listByResource(@RequestParam String resourceId);

    @Operation(summary = "转码入队（写入 resource_task 并调用转码平台）")
    @PostMapping("/api/material/task/transcode/enqueue")
    MaterialResourceTaskVO enqueueTranscode(@Valid @RequestBody MaterialTranscodeEnqueueDTO req);

    @Operation(summary = "转码失败重试（同一主记录，更新外部任务ID与状态）")
    @PostMapping("/api/material/task/transcode/retry")
    MaterialResourceTaskVO retryTranscode(@RequestParam String taskId);
}
