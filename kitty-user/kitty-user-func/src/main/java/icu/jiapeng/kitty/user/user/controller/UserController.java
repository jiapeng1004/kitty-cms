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

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.captcha.CaptchaService;
import icu.jiapeng.kitty.user.captcha.CaptchaServiceType;
import icu.jiapeng.kitty.user.cfg.TenantConfigEnum;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.user.api.UserApi;
import icu.jiapeng.kitty.user.user.dto.UserLoginParam;
import icu.jiapeng.kitty.user.user.dto.UserQueryPageDTO;
import icu.jiapeng.kitty.user.user.dto.UserRegister;
import icu.jiapeng.kitty.user.user.dto.UserUpdateDTO;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import icu.jiapeng.kitty.user.user.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController implements UserApi {

    private final KtUserService ktUserService;
    private final KtConfigService ktConfigService;

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

    @Override
    @PostMapping("/api/user/logout")
    public void logout() {
        ktUserService.logout();
    }

    @Operation(summary = "验证码")
    @GetMapping("/api/user/captcha")
    @Override
    public void captcha(HttpServletRequest request, HttpServletResponse response) {
        CaptchaService service = resolveCaptchaService(request);
        var result = service.generateCaptcha();
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        try {
            response.getOutputStream().write(result.getImageBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析验证码服务：优先请求参数，其次配置表，再兜底枚举默认；通过 {@link CaptchaService.Factory#resolve(String)} 解析。
     */
    private CaptchaService resolveCaptchaService(HttpServletRequest request) {
        String type = request.getParameter(CaptchaService.CAPTCHA_TYPE_PARAM_NAME);
        if (StrUtil.isBlank(type)) {
            type = ktConfigService.getVal(TenantConfigEnum.CAPTCHA_SERVICE_TYPE);
        }
        if (StrUtil.isBlank(type)) {
            type = CaptchaServiceType.HUTOOL_REDIS.getType();
        }
        return CaptchaService.Factory.resolve(type.trim())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
    }

    @Override
    @Operation(summary = "分页查询用户（管理端）")
    @GetMapping("/api/user/query")
    public PageRespVo<UserListVO> query(UserQueryPageDTO query) {
        return ktUserService.query(query);
    }

    @Override
    @Operation(summary = "根据ID获取用户详情（管理端）")
    @GetMapping("/api/user/{id}")
    public UserListVO getDetail(@NotBlank @PathVariable String id) {
        return ktUserService.getDetail(id);
    }

    @Override
    @Operation(summary = "更新用户（管理端）")
    @PutMapping("/api/user/{id}")
    public boolean update(@NotBlank @PathVariable String id, @Valid @RequestBody UserUpdateDTO dto) {
        return ktUserService.update(id, dto);
    }
}
