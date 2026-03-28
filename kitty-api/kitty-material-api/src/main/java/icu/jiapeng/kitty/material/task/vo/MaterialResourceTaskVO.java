package icu.jiapeng.kitty.material.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资源任务 VO")
public class MaterialResourceTaskVO {

    private String id;
    private String resourceId;
    private String resourceTitle;
    private String taskType;
    private String thirdTaskId;
    private Integer progress;
    private String status;
    private String inputType;
    private String inputPath;
    private String materialStrategyId;
    private String strategyName;
}
