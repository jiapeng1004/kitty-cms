package icu.jiapeng.kitty.transcoder.func.magic;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.engine.MediaStepOps;
import icu.jiapeng.kitty.transcoder.func.engine.TranscodeEngine;
import icu.jiapeng.kitty.transcoder.func.file.HttpFileHandler;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.InputType;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.TaskStatus;
import icu.jiapeng.kitty.transcoder.func.task.TaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Consumer;
import java.util.UUID;

@Slf4j
@Service
public class MagicServiceImpl implements MagicService {

    private static final String TASK_TYPE_EXTRACT = "MAGIC_EXTRACT_FRAMES";
    private static final String TASK_TYPE_IMAGE = "MAGIC_IMAGE_CONVERT";
    private static final String TASK_TYPE_TRANSCODE = "MAGIC_SYNC_TRANSCODE";

    @Resource
    private TaskService taskService;
    @Resource
    private MediaStepOps mediaStepOps;
    @Resource
    private TranscodeEngine transcodeEngine;
    @Resource
    private TranscodeConfig transcodeConfig;

    @Override
    public TaskVO extractFrames(MagicExtractFramesRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String inputPath = request.getInputPath();
        if (inputPath == null || inputPath.isBlank()) throw new IllegalArgumentException("输入不能为空");
        String inputType = StrUtil.isNotBlank(request.getInputType()) ? request.getInputType() : InputType.DISK;

        // JavaCV/FFmpeg 原生支持 HTTP/HTTPS URL，无需预下载
        String effectiveInput = inputPath;
        // 抽帧输出 base：单帧为 workDir/taskId_frame.png，多帧为 workDir/taskId/frame_0.png, frame_1.png, ...
        String resolvedOutputBase = null;
        if (InputType.HTTP.equalsIgnoreCase(inputType) && (inputPath.startsWith("http://") || inputPath.startsWith("https://"))) {
            resolvedOutputBase = java.nio.file.Paths.get(transcodeConfig.getWorkDir(), taskId).toAbsolutePath().toString();
        } else {
            File f = new File(inputPath);
            if (!f.exists()) throw new IllegalArgumentException("本地文件不存在：" + inputPath);
        }
        taskService.createMagicTaskRecord(taskId, TASK_TYPE_EXTRACT, inputType, inputPath);

        try {
            StrategyStepVO step = new StrategyStepVO();
            step.setFrameInterval(request.getFrameInterval() != null ? request.getFrameInterval() : 30);
            step.setExtractFrameCount(request.getFrameCount() != null ? request.getFrameCount() : 1);
            step.setExtractOutputFormat("png".equalsIgnoreCase(request.getOutputFormat()) ? "png" : "jpg");

            String workDir = transcodeConfig.getWorkDir();
            String outPath = mediaStepOps.doExtractFrames(effectiveInput, step, "_magic", resolvedOutputBase, workDir);
            String outputHttpUrl = buildOutputHttpUrl(outPath);
            taskService.completeMagicTask(taskId, outPath, outputHttpUrl);
            return taskService.getTask(taskId);
        } catch (Exception e) {
            log.error("魔法抽帧失败 taskId={}", taskId, e);
            taskService.updateTaskError(taskId, "抽帧失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED, 0);
            throw new RuntimeException(e);
        }
    }

    @Override
    public TaskVO imageConvert(MagicImageConvertRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String inputPath = request.getInputPath();
        if (inputPath == null || inputPath.isBlank()) throw new IllegalArgumentException("输入不能为空");
        String inputType = StrUtil.isNotBlank(request.getInputType()) ? request.getInputType() : InputType.DISK;

        String localPath = resolveInput(inputPath, inputType, taskId);
        taskService.createMagicTaskRecord(taskId, TASK_TYPE_IMAGE, inputType, inputPath);

        try {
            StrategyStepVO step = new StrategyStepVO();
            step.setImageTargetFormat(StrUtil.isNotBlank(request.getTargetFormat()) ? request.getTargetFormat() : "webp");
            step.setImageQuality(request.getQuality() != null ? request.getQuality() : 85);
            step.setImageResize(request.getResize());

            String workDir = transcodeConfig.getWorkDir();
            String outPath = mediaStepOps.doImageConvert(localPath, step, "_magic", null, workDir);
            String outputHttpUrl = buildOutputHttpUrl(outPath);
            taskService.completeMagicTask(taskId, outPath, outputHttpUrl);
            return taskService.getTask(taskId);
        } catch (Exception e) {
            log.error("魔法图转失败 taskId={}", taskId, e);
            taskService.updateTaskError(taskId, "图转失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED, 0);
            throw new RuntimeException(e);
        }
    }

    @Override
    public TaskVO transcode(MagicTranscodeRequest request, Consumer<ProgressVO> progressConsumer) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String inputPath = request.getInputPath();
        if (inputPath == null || inputPath.isBlank()) throw new IllegalArgumentException("输入不能为空");
        String inputType = StrUtil.isNotBlank(request.getInputType()) ? request.getInputType() : InputType.DISK;

        String localPath = resolveInput(inputPath, inputType, taskId);
        taskService.createMagicTaskRecord(taskId, TASK_TYPE_TRANSCODE, inputType, inputPath);

        try {
            TranscodeEngine.ProgressCallback cb = progressConsumer != null
                    ? (tid, status, progress, stepList) -> {
                taskService.updateTaskStatus(tid, status, progress, stepList);
                progressConsumer.accept(taskService.getProgress(tid));
            }
                    : (tid, status, progress, stepList) -> taskService.updateTaskStatus(tid, status, progress, stepList);

            String outPath = transcodeEngine.transcodeSingleTarget(
                    taskId, localPath,
                    request.getTargetFormat(), request.getResolution(),
                    request.getBitrate(), request.getFrameRate(),
                    cb);
            String outputHttpUrl = buildOutputHttpUrl(outPath);
            taskService.completeMagicTask(taskId, outPath, outputHttpUrl);
            if (progressConsumer != null) {
                progressConsumer.accept(taskService.getProgress(taskId));
            }
            return taskService.getTask(taskId);
        } catch (Exception e) {
            log.error("魔法转码失败 taskId={}", taskId, e);
            taskService.updateTaskError(taskId, "转码失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED, 0);
            throw new RuntimeException(e);
        }
    }

    private String resolveInput(String inputPath, String inputType, String taskId) {
        if (InputType.HTTP.equalsIgnoreCase(inputType) && (inputPath.startsWith("http://") || inputPath.startsWith("https://"))) {
            return HttpFileHandler.downloadToTemp(inputPath, taskId, transcodeConfig.getWorkDir());
        }
        File f = new File(inputPath);
        if (!f.exists()) throw new IllegalArgumentException("本地文件不存在：" + inputPath);
        return inputPath;
    }

    private String buildOutputHttpUrl(String outputPath) {
        String prefix = transcodeConfig.getOutput() != null && transcodeConfig.getOutput().getHttpPrefix() != null
                ? transcodeConfig.getOutput().getHttpPrefix() : "";
        if (prefix.isEmpty()) return null;
        if (outputPath == null) return null;
        if (outputPath.startsWith(prefix)) return outputPath;
        return prefix.endsWith("/") ? prefix + outputPath : prefix + "/" + outputPath;
    }
}
