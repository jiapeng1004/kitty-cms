package icu.jiapeng.kitty.transcoder.func.entity;

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
    private String inputType;
    private String inputPath;
    private String strategyId;
    private String status;
    private Integer progress;
    private String outputPath;
    private String outputHttpUrl;
    private String errorMessage;
    private Integer priority;
    private Integer retryCount;
    private String notificationConfig;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String createdByAk;
}
