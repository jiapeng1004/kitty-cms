package icu.jiapeng.kitty.material.transcode.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_catalog_transcode_strategy_bind")
public class KtCatalogTranscodeStrategyBind extends CommonEntity {

    @TableField("catalog_id")
    private String catalogId;

    @TableField("strategy_id")
    private String strategyId;

    @TableField("resource_type")
    private Integer resourceType;

    @TableField("sort_num")
    private Integer sortNum;
}
