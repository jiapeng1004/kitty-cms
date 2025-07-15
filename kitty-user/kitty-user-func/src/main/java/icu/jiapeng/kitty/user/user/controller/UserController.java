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
package icu.jiapeng.kitty.user.user.controller;

import icu.jiapeng.kitty.user.user.api.UserApi;
import icu.jiapeng.kitty.user.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.user.dto.UserRegister;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {

    private final KtUserService ktUserService;

    @Override
    @PostMapping("/api/user/register")
    public String register(@RequestBody UserRegister userRegister) {
        return ktUserService.register(userRegister);
    }

    @PostMapping("/api/user/login")
    @Override
    public LoginResultVo login(@RequestBody UserLoginParam userLoginParam) {
        return ktUserService.login(userLoginParam);
    }

    @Operation(summary = "验证码")
    @GetMapping("/api/user/captcha")
    @Override
    public void captcha(HttpServletRequest request, HttpServletResponse response) {
        ktUserService.captcha(request, response);
    }
}
