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
package icu.jiapeng.kitty.user.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户注册参数
 */
@Getter
@Setter
@Schema(description = "用户注册参数")
public class UserLoginParam {

    @NotBlank(message = "{user.username.not.blank}")
    @Schema(description = "账号", maxLength = 15, minLength = 2, example = "贾鹏大大")
    private String username;
    @Schema(description = "密码(加密)", maxLength = 20, minLength = 6, example = "123456")
    @NotBlank(message = "{user.password.not.blank}")
    private String password;
    @Schema(description = "验证码", maxLength = 4, minLength = 4, example = "1234")
    @NotBlank(message = "{captcha.not.blank}")
    private String captcha;
}