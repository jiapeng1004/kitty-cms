/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.api.cfg.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 配置分类创建参数
 *
 * @author jiapeng
 */
@Data
@Schema(description = "配置分类创建参数")
public class ClassCreateDTO {

    @NotBlank(message = "config.class.name.not.blank")
    @Length(max = 20, min = 2, message = "config.class.name.length.2-10")
    @Schema(description = "类名")
    private String className;

    @Length(max = 100, min = 2, message = "config.class.desc.length.2-100")
    @Schema(description = "类描述", nullable = true)
    private String classDesc;

    @Length(max = 32, min = 2, message = "config.class.owner.length.2-20")
    @Schema(description = "分类所有者", nullable = true)
    private String owner;
}
