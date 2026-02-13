package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.api.CreateTaskRequest;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

public interface TaskService {

    /**
     * 创建转码任务
     * @param request 创建任务请求
     * @return 任务ID
     */
    String createTask(CreateTaskRequest request);

    /**
     * 查询转码任务
     * @param taskId 任务ID
     * @return 任务详情
     */
    TaskVO getTask(String taskId);

    /**
     * 取消转码任务
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTask(String taskId);

    /**
     * 处理转码任务
     * @param taskId 任务ID
     */
    void processTask(String taskId);

    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 状态
     * @param progress 进度
     */
    void updateTaskStatus(String taskId, String status, int progress);

    /**
     * 更新任务错误信息
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    void updateTaskError(String taskId, String errorMessage);

    /**
     * 获取转码进度
     * @param taskId 任务ID
     * @return 进度信息
     */
    ProgressVO getProgress(String taskId);

    /**
     * 获取SSE实时进度
     * @param taskId 任务ID
     * @return SSE事件流
     */
    SseEmitter getProgressSSE(String taskId);

}