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

import java.util.Map;

/**
 * 配置项更新参数
 *
 * @author jiapeng
 */
@Data
@Schema(description = "配置项更新参数")
public class ConfigUpdateDTO {

    @Length(max = 64)
    @Schema(description = "配置项名称", nullable = true)
    private String configName;

    @Length(max = 256)
    @Schema(description = "配置项描述", nullable = true)
    private String configDesc;

    @Schema(description = "配置方式 TEXT|TEXTAREA|RADIO|CHECKBOX", nullable = true)
    private String configWay;

    @Schema(description = "所属分类 id", nullable = true)
    private String classId;

    @Schema(description = "配置默认值", nullable = true)
    private String configDefault;

    @Schema(description = "配置值", nullable = true)
    private String configValue;

    @Schema(description = "枚举值(configWay=RADIO/CHECKBOX时有效)", nullable = true)
    private Map<String, String> configEnum;
}
