package icu.jiapeng.kitty.transcoder.func.strategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 策略导出/导入的语义化格式，与数据库结构解耦。
 * 支持 format_version 便于未来字段扩充/收缩时兼容。
 * 未知字段在导入时忽略，缺失字段使用默认值。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyExportFormat {

    /**
     * 格式版本，当前为 1。未来字段变更时可递增并做迁移。
     */
    @JsonProperty("format_version")
    private Integer formatVersion = 1;

    /**
     * 策略ID。导出时写入，导入时用于保持 ID 不变（存在则更新，不存在则创建后设置）。
     */
    @JsonProperty("strategy_id")
    private String strategyId;

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略级工作目录
     */
    @JsonProperty("work_dir")
    private String workDir;

    /**
     * 步骤列表
     */
    private List<StrategyStepExport> steps;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StrategyStepExport {
        @JsonProperty("step_id")
        private Integer stepId;

        private String type;
        private String depends;

        @JsonProperty("input_template")
        private String inputTemplate;

        @JsonProperty("output_template")
        private String outputTemplate;

        @JsonProperty("target_format")
        private String targetFormat;

        private String resolution;
        private Integer bitrate;

        @JsonProperty("frame_rate")
        private Integer frameRate;

        private String encoder;

        @JsonProperty("frame_interval")
        private Integer frameInterval;

        @JsonProperty("extract_frame_count")
        private Integer extractFrameCount;

        @JsonProperty("extract_output_format")
        private String extractOutputFormat;

        @JsonProperty("sprite_columns")
        private Integer spriteColumns;

        @JsonProperty("sprite_rows")
        private Integer spriteRows;

        @JsonProperty("sprite_scale")
        private Integer spriteScale;

        @JsonProperty("image_target_format")
        private String imageTargetFormat;

        @JsonProperty("image_quality")
        private Integer imageQuality;

        @JsonProperty("image_resize")
        private String imageResize;

        private String condition;

        @JsonProperty("strategy_id_when_true")
        private String strategyIdWhenTrue;

        @JsonProperty("strategy_id_when_false")
        private String strategyIdWhenFalse;
    }
}
