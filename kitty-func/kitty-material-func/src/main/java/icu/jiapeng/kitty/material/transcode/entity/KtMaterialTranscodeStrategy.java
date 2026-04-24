package icu.jiapeng.kitty.material.transcode.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import icu.jiapeng.kitty.material.resource.constants.ResourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    /** 与 {@link ResourceTypeEnum#getType()} 一致；空=兼容旧数据 */
    @TableField("resource_type")
    private Integer resourceType;

    /** 1=该 {@link #resourceType} 下全局默认（同类至多一条，由应用层保证） */
    @TableField("is_global_default")
    private Integer isGlobalDefault;
}
