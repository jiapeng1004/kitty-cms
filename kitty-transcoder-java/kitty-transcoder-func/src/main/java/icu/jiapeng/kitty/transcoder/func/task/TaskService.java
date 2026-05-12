package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.api.CreateTaskRequest;
import icu.jiapeng.kitty.transcoder.api.ListTasksResponse;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

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
     * 分页查询任务列表
     * @param req 查询参数（taskId、filename、timeFrom、timeTo、strategyId、status、taskType、sortBy、sortOrder）
     * @return 分页结果（list、total、page、pageSize）
     */
    ListTasksResponse listTasks(icu.jiapeng.kitty.transcoder.api.ListTasksRequest req);

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
     *
     * @param taskId 任务ID
     * @return SSE事件流
     */
    Flux<ServerSentEvent<ProgressVO>> getProgressSSE(String taskId);

    /**
     * 获取全任务进度 SSE 流，任意任务进度更新时广播。返回 Flux 以适配 HttpExchange 声明式客户端。
     * @return SSE 事件流
     */
    Flux<ServerSentEvent<ProgressVO>> getProgressStream();

    /**
     * 创建魔法任务记录（不入队，无策略，仅用于列表展示）
     * @param taskId 任务ID
     * @param taskType 任务类型
     * @param inputType 输入类型
     * @param inputPath 输入路径
     */
    void createMagicTaskRecord(String taskId, String taskType, String inputType, String inputPath);

    /**
     * 完成魔法任务（含输出路径、HTTP URL）
     */
    void completeMagicTask(String taskId, String outputPath, String outputHttpUrl);

    /**
     * 任务整体重试：将失败/取消的任务重置为 PENDING 并重新入队。
     *
     * @param taskId 任务ID
     * @return 是否已入队（仅 FAILED/CANCELLED 可重试）
     */
    boolean retryTask(String taskId);

    /**
     * 单步骤重试：重新执行指定步骤，依赖步骤输出从已保存的 stepOutputs 读取。仅当任务已有 stepOutputs（如已成功完成过）时可调用。
     *
     * @param taskId 任务ID
     * @param stepId 步骤ID
     * @return 该步骤新的输出路径；失败抛异常
     */
    String retryStep(String taskId, int stepId) throws Exception;

}