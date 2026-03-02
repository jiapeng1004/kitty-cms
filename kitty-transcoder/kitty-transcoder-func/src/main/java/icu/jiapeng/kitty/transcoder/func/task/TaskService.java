package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.api.CreateTaskRequest;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface TaskService {

    /**
     * 创建转码任务（可选审计：创建方 AccessKeyId）
     * @param request 创建任务请求
     * @param createdByAk 创建方 AccessKeyId（可为 null）
     * @return 任务ID
     */
    String createTask(CreateTaskRequest request, String createdByAk);

    /**
     * 查询转码任务
     * @param taskId 任务ID
     * @return 任务详情
     */
    TaskVO getTask(String taskId);

    /**
     * 分页查询任务列表（按创建时间倒序）
     * @param page 页码从1开始
     * @param size 每页条数
     * @param taskId 任务ID（可选，支持模糊检索）
     * @return 任务列表
     */
    List<TaskVO> listTasks(int page, int size, String taskId);

    /**
     * 取消转码任务
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTask(String taskId);

    /**
     * 删除任务记录（物理删除）
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(String taskId);

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
     * 更新任务状态（含分步进度）
     */
    void updateTaskStatus(String taskId, String status, int progress, java.util.List<icu.jiapeng.kitty.transcoder.api.StepProgressItem> stepProgressList);

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
     * 获取SSE实时进度（单任务）
     * @param taskId 任务ID
     * @return SSE事件流
     */
    SseEmitter getProgressSSE(String taskId);

    /**
     * 获取全任务进度 SSE 流，任意任务进度更新时广播
     * @return SSE事件流
     */
    SseEmitter getProgressStream();

}