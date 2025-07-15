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
package icu.jiapeng.kitty.user.user.api;

import icu.jiapeng.kitty.user.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.user.dto.UserRegister;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;


@Tag(name = "用户API")
@HttpExchange
@Validated
public interface UserApi {
    @Operation(summary = "用户注册")
    @PostExchange("/api/user/register")
    @ApiResponse(description = "用户id")
    String register(@Valid @RequestBody UserRegister userRegister);

    @Operation(summary = "用户登录")
    @PostExchange("/api/user/login")
    LoginResultVo login(@Valid @RequestBody UserLoginParam userLoginParam);

    @Operation(summary = "验证码")
    @GetExchange("/api/user/captcha")
    void captcha(HttpServletRequest request, HttpServletResponse response);
}