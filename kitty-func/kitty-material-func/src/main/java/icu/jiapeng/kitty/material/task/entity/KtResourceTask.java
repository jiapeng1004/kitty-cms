package icu.jiapeng.kitty.material.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 资源任务表（与 DDL resource_task 一致，含转码入参扩展列）。
 */
@Data
@TableName("kt_resource_task")
@EqualsAndHashCode(callSuper = true)
public class KtResourceTask extends CommonEntity {

    @TableField("resource_id")
    private String resourceId;

    @TableField("resource_title")
    private String resourceTitle;

    @TableField("task_type")
    private String taskType;

    @TableField("third_task_id")
    private String thirdTaskId;

    @TableField("progress")
    private Integer progress;

    @TableField("status")
    private String status;

    @TableField("input_type")
    private String inputType;

    @TableField("input_path")
    private String inputPath;

    @TableField("material_strategy_id")
    private String materialStrategyId;

    @TableField("deleted")
    private Integer deleted;
}
