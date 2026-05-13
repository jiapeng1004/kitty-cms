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
package icu.jiapeng.kitty.user.auth.open;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.user.api.open.OpenAuthCallbackParams;
import icu.jiapeng.kitty.user.api.open.OpenAuthRenderParams;
import icu.jiapeng.kitty.user.api.open.api.OpenAuthApi;
import icu.jiapeng.kitty.user.cfg.TenantConfigEnum;
import icu.jiapeng.kitty.user.api.cfg.dto.GetValDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.api.user.vo.LoginResultVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 开放认证（三方登录）：跳转授权、回调登录。
 * URL 仍为 /oauth2 以兼容前端与第三方回调；接口不依赖 JustAuth，由实现（如 JustAuth）完成具体协议。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OpenAuthController implements OpenAuthApi {

    private final OpenAuthService openAuthService;
    private final KtConfigService ktConfigService;

    /**
     * 预检：检查该 source 平台是否已配置并就绪，登录页可先调此接口再决定是否展示按钮或跳转 render。
     *
     * @param source 平台：feishu、dingtalk、github、google、microsoft、wechat
     * @return 就绪时返回 { "ready": true }，未配置时抛异常
     */
    @Override
    public Boolean check(@PathVariable String source) {
        if (!openAuthService.isSourceReady(source)) {
            throw new BizException("该登录方式未配置或不可用", ResultStatus.PARAM_ERROR);
        }
        return true;
    }

    /**
     * 跳转到第三方授权页。
     *
     * @param source   平台：feishu、dingtalk、github、google、microsoft、wechat
     * @param params   渲染参数（如 next），由请求 query 绑定，next 会拼到 redirect_uri 后
     * @param response 用于重定向
     */
    @Override
    public void render(
            @PathVariable String source,
            OpenAuthRenderParams params,
            HttpServletResponse response
    ) throws IOException {
        Optional<String> urlOpt = openAuthService.getAuthorizeUrl(source, params);
        if (urlOpt.isEmpty()) {
            throw new BizException("该登录方式未配置或不可用", ResultStatus.PARAM_ERROR);
        }
        response.sendRedirect(urlOpt.get());
    }

    /**
     * 第三方授权回调：用 code/state 换用户信息并完成本地登录，重定向到前端并携带 token。
     *
     * @param source   平台：feishu、dingtalk 等
     * @param params   回调参数（code、state），由请求 query 绑定
     * @param response 用于重定向
     */
    @Override
    public void callback(
            @PathVariable String source,
            OpenAuthCallbackParams params,
            HttpServletResponse response
    ) throws IOException {
        callbackNext(source, null, params, response);
    }

    /**
     * 第三方授权回调：用 code/state 换用户信息并完成本地登录，重定向到前端并携带 token。
     *
     * @param source   平台：feishu、dingtalk 等
     * @param params   回调参数（code、state），由请求 query 绑定
     * @param response 用于重定向
     */
    @Override
    public void callbackNext(
            @PathVariable String source,
            @PathVariable String next,
            OpenAuthCallbackParams params,
            HttpServletResponse response
    ) throws IOException {
        if (params != null) {
            params.setNext(next);
        }
        Optional<LoginResultVo> loginResult = openAuthService.handleCallback(source, params);
        if (loginResult.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "第三方登录失败或未配置");
            return;
        }
        String token = loginResult.get().getToken();
        String redirectUri;
        if (StrUtil.isNotBlank(next)) {
            redirectUri = ktConfigService.getVal(new GetValDTO().setConfigKey(TenantConfigEnum.OPEN_AUTH_REDIRECT_AFTER_LOGIN.getKey() + "." + next));
        } else {
            String baseRedirect = ktConfigService.getVal(TenantConfigEnum.OPEN_AUTH_REDIRECT_AFTER_LOGIN);
            redirectUri = StrUtil.isNotBlank(baseRedirect)
                    ? baseRedirect + (baseRedirect.contains("?") ? "&" : "?") + "oauth2=1&token=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8)
                    : "/login?oauth2=1&token=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
        }
        response.sendRedirect(redirectUri);
    }
}
