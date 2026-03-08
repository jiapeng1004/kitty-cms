package icu.jiapeng.kitty.transcoder.func.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Data
@TableName("transcode_task")
@FieldNameConstants
public class TranscodeTask {

    @TableId
    private String id;
    private String taskType;
    private String inputType;
    private String inputPath;
    private Long strategyId;
    private String status;
    private Integer progress;
    private String progressDetail;
    private String outputPath;
    private String outputHttpUrl;
    /** 各步骤输出路径 JSON：{"1":"path/1080p.mp4","2":"path/480p.mp4"}，用于步骤级重试 */
    @TableField("step_outputs")
    private String stepOutputs;
    private String errorMessage;
    private String watermarkUrl;
    private String watermarkPosition;
    private Integer priority;
    private Integer retryCount;
    @TableField("notification_config")
    private String notificationConfig;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String createdByAk;
}
