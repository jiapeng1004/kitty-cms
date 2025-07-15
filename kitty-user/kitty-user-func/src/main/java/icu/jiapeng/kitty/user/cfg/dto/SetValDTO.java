package icu.jiapeng.kitty.user.cfg.dto;


import icu.jiapeng.kitty.common.core.valid.InEnum;
import icu.jiapeng.kitty.user.cfg.constants.ConfigWay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@Data
@Schema(description = "设置值参数")
public class SetValDTO {
    /**
     * 配置键id
     */
    @Schema(description = "配置键id", nullable = true)
    private String id;

    /**
     * 配置键key
     */
    @Schema(description = "配置键key", nullable = true)
    private String configKey;

    /**
     * 配置值
     */
    @Schema(description = "配置值", nullable = true)
    private String configValue;

    /**
     * 配置项名称
     */
    @Schema(description = "配置项名称", nullable = true)
    private String configName;

    /**
     * 配置项详细描述
     */
    @Schema(description = "配置项详细描述", nullable = true)
    private String configDesc;

    /**
     * 配置方式
     * {@link icu.jiapeng.kitty.user.cfg.constants.ConfigWay}
     *
     */
    @Schema(description = "配置方式", nullable = true)
    @InEnum(value = ConfigWay.class)
    private String configWay;

    /**
     * 配置项枚举值configWay=RADIO/CHECKBOX有效
     * {@link java.util.Map} Map<String,String> 枚举值-枚举label
     */
    @Schema(description = "配置项枚举值configWay=RADIO/CHECKBOX有效", nullable = true)
    private Map<String, String> configEnum;
}
