package icu.jiapeng.kitty.transcoder.func.engine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 步骤输入/输出路径模板解析，支持占位符：$TASK_ID、$DATE、$TIME、$DATE_TIME、$TASK_INPUT、$STEP_OUTPUT_1、$STEP_INDEX、$WORK_DIR。
 * 步骤序号从 1 开始。$DATE=年/月/日 文件夹层级，$DATE_TIME=年/月/日/时分秒。
 */
public final class StepTemplateResolver {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    /**
     * 解析模板字符串。占位符：
     * $TASK_ID 任务ID；$DATE 年/月/日（如 2026/03/03）；$TIME 时分秒；$DATE_TIME 年/月/日/时分秒；
     * $TASK_INPUT 任务输入路径；$STEP_OUTPUT_1、$STEP_OUTPUT_2...；$STEP_INDEX；$WORK_DIR。
     */
    public static String resolve(String template, String taskId, String taskInputPath,
                                 Map<Integer, String> stepOutputs, int stepIndex, String workDir) {
        if (template == null || template.isBlank()) return template;
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DATE_FMT);
        String timePart = now.format(TIME_FMT);
        String dateTimePath = datePath + "/" + timePart;
        String s = template;
        if (taskId != null) s = s.replace("$TASK_ID", taskId);
        s = s.replace("$DATE_TIME", dateTimePath);
        s = s.replace("$DATETIME", dateTimePath);
        s = s.replace("$DATE", datePath);
        s = s.replace("$TIME", timePart);
        if (taskInputPath != null) s = s.replace("$TASK_INPUT", taskInputPath);
        if (stepOutputs != null) {
            for (Map.Entry<Integer, String> e : stepOutputs.entrySet()) {
                if (e.getValue() != null) s = s.replace("$STEP_OUTPUT_" + e.getKey(), e.getValue());
            }
        }
        s = s.replace("$STEP_INDEX", String.valueOf(stepIndex));
        if (workDir != null) s = s.replace("$WORK_DIR", workDir.replace("\\", "/"));
        return s;
    }
}
