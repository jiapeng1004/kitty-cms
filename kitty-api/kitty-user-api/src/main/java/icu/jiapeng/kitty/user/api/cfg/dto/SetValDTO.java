package icu.jiapeng.kitty.user.api.cfg.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
}
