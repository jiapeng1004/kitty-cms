package icu.jiapeng.kitty.user.cfg.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 *
 *
 * @author jiapeng
 * @since 2025/12/21
 */
@Schema(description = "创建配置类参数")
public class ClassCreateDTO {
    /**
     * 类名
     */
    @Schema(description = "类名")
    @NotBlank(message = "config.class.name.not.blank")
    @Length(max = 20, min = 2, message = "config.class.name.length.2-10")
    private String className;

    /**
     * 类描述
     */
    @Schema(description = "类描述", nullable = true)
    @Length(max = 100, min = 2, message = "config.class.desc.length.2-100")
    private String classDesc;

    /**
     * 分类所有者
     * public公共
     */
    @Schema(description = "分类所有者", nullable = true)
    @Length(max = 32, min = 2, message = "config.class.owner.length.2-20")
    private String owner;
}
