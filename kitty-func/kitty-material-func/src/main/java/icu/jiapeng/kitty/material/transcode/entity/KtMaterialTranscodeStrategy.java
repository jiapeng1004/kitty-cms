package icu.jiapeng.kitty.material.transcode.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_material_transcode_strategy")
public class KtMaterialTranscodeStrategy extends CommonEntity {

    @TableField("name")
    private String name;

    @TableField("platform_code")
    private String platformCode;

    @TableField("external_strategy_id")
    private String externalStrategyId;

    @TableField("params_json")
    private String paramsJson;

    @TableField("enabled")
    private Integer enabled;
}
