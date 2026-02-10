package icu.jiapeng.kitty.user.cfg.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 获取值
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@Getter
@Setter
@Schema(description = "获取值的参数")
@Accessors(chain = true)
public class GetValDTO {

    /**
     * 配置key
     */
    @Schema(description = "配置key")
    @NotBlank(message = "config.configKey.not.blank")
    private String configKey;

}
