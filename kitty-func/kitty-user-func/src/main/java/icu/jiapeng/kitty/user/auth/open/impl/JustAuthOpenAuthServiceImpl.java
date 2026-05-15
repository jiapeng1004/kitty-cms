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
package icu.jiapeng.kitty.user.auth.open.impl;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.user.api.open.OpenAuthCallbackParams;
import icu.jiapeng.kitty.user.auth.open.OpenAuthCallbackService;
import icu.jiapeng.kitty.user.api.open.OpenAuthRenderParams;
import icu.jiapeng.kitty.user.auth.open.OpenAuthService;
import icu.jiapeng.kitty.user.cfg.TenantConfigEnum;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.common.core.util.CommonServletUtil;
import icu.jiapeng.kitty.user.api.user.vo.LoginResultVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 开放认证服务 JustAuth 实现：仅在此类中依赖 JustAuth，接口层不暴露 JustAuth 类型。
 */
@Slf4j
@Service
public class JustAuthOpenAuthServiceImpl implements OpenAuthService {

    @Resource
    private KtConfigService ktConfigService;
    @Resource
    private OpenAuthCallbackService openAuthCallbackService;
    @Resource
    private RedisAuthStateCache authStateCache;

    @Override
    public boolean isSourceReady(String source) {
        if (source == null) return false;
        return switch (source.trim().toLowerCase()) {
            case "feishu" ->
                    StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_FEISHU_CLIENT_ID)) && StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_FEISHU_CLIENT_SECRET));
            case "dingtalk" ->
                    StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_DINGTALK_CLIENT_ID)) && StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_DINGTALK_CLIENT_SECRET));
            case "github" ->
                    StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_GITHUB_CLIENT_ID)) && StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_GITHUB_CLIENT_SECRET));
            case "google" ->
                    StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_GOOGLE_CLIENT_ID)) && StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_GOOGLE_CLIENT_SECRET));
            case "microsoft" ->
                    StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_MICROSOFT_CLIENT_ID)) && StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_MICROSOFT_CLIENT_SECRET));
            case "wechat" ->
                    StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_WECHAT_CLIENT_ID)) && StrUtil.isNotBlank(getVal(TenantConfigEnum.OPEN_AUTH_WECHAT_CLIENT_SECRET));
            default -> false;
        };
    }

    @Override
    public Optional<String> getAuthorizeUrl(String source, OpenAuthRenderParams params) {
        String baseUrl = CommonServletUtil.getCurrentBaseUrl();
        if (StrUtil.isBlank(baseUrl)) return Optional.empty();
        String next = params != null ? params.getNext() : null;
        Optional<AuthRequest> reqOpt = getAuthRequest(source, next, baseUrl);
        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }
        String state = AuthStateUtils.createState();
        String url = reqOpt.get().authorize(state);
        return Optional.ofNullable(url);
    }

    @Override
    public Optional<LoginResultVo> handleCallback(String source, OpenAuthCallbackParams params) {
        if (params == null || StrUtil.isBlank(params.getCode())) {
            return Optional.empty();
        }
        String baseUrl = CommonServletUtil.getCurrentBaseUrl();
        if (StrUtil.isBlank(baseUrl)) return Optional.empty();
        String next = params.getNext();
        Optional<AuthRequest> reqOpt = getAuthRequest(source, next, baseUrl);
        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }
        AuthCallback callback = new AuthCallback();
        callback.setCode(params.getCode());
        callback.setState(params.getState());
        AuthResponse<AuthUser> authResponse = reqOpt.get().login(callback);
        if (authResponse == null || !authResponse.ok()) {
            log.warn("开放认证 callback 失败: source={}, {}", source, authResponse != null ? authResponse.getMsg() : "null");
            return Optional.empty();
        }
        AuthUser authUser = authResponse.getData();
        return openAuthCallbackService.loginByOAuthUser(
                source,
                authUser.getUuid(),
                authUser.getNickname(),
                authUser.getUsername()
        );
    }

    private Optional<AuthRequest> getAuthRequest(String source, String next, String callbackBaseUrl) {
        if (source == null || StrUtil.isBlank(callbackBaseUrl)) {
            return Optional.empty();
        }
        String redirectUri = PathUtil.builderPath(callbackBaseUrl, "/open/oauth2/auth/callback", source, next);
        return switch (source.trim().toLowerCase()) {
            case "feishu" -> buildFeishuRequest(redirectUri);
            case "dingtalk" -> buildDingTalkRequest(redirectUri);
            case "github" -> buildGithubRequest(redirectUri);
            case "google" -> buildGoogleRequest(redirectUri);
            case "microsoft" -> buildMicrosoftRequest(redirectUri);
            case "wechat" -> buildWeChatRequest(redirectUri);
            default -> Optional.empty();
        };
    }

    private Optional<AuthRequest> buildFeishuRequest(String redirectUri) {
        String clientId = getVal(TenantConfigEnum.OPEN_AUTH_FEISHU_CLIENT_ID);
        String clientSecret = getVal(TenantConfigEnum.OPEN_AUTH_FEISHU_CLIENT_SECRET);
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            log.debug("飞书开放认证配置不完整，未启用");
            return Optional.empty();
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();
        return Optional.of(new AuthFeishuRequest(config, authStateCache));
    }

    private Optional<AuthRequest> buildDingTalkRequest(String redirectUri) {
        String clientId = getVal(TenantConfigEnum.OPEN_AUTH_DINGTALK_CLIENT_ID);
        String clientSecret = getVal(TenantConfigEnum.OPEN_AUTH_DINGTALK_CLIENT_SECRET);
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            log.debug("钉钉开放认证配置不完整，未启用");
            return Optional.empty();
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();
        return Optional.of(new AuthDingTalkRequest(config, authStateCache));
    }

    private Optional<AuthRequest> buildGithubRequest(String redirectUri) {
        String clientId = getVal(TenantConfigEnum.OPEN_AUTH_GITHUB_CLIENT_ID);
        String clientSecret = getVal(TenantConfigEnum.OPEN_AUTH_GITHUB_CLIENT_SECRET);
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            log.debug("GitHub 开放认证配置不完整，未启用");
            return Optional.empty();
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .ignoreCheckState(true)
                .build();
        return Optional.of(new AuthGithubRequest(config, authStateCache));
    }

    private Optional<AuthRequest> buildGoogleRequest(String redirectUri) {
        String clientId = getVal(TenantConfigEnum.OPEN_AUTH_GOOGLE_CLIENT_ID);
        String clientSecret = getVal(TenantConfigEnum.OPEN_AUTH_GOOGLE_CLIENT_SECRET);
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            log.debug("Google 开放认证配置不完整，未启用");
            return Optional.empty();
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();
        return Optional.of(new AuthGoogleRequest(config, authStateCache));
    }

    private Optional<AuthRequest> buildMicrosoftRequest(String redirectUri) {
        String clientId = getVal(TenantConfigEnum.OPEN_AUTH_MICROSOFT_CLIENT_ID);
        String clientSecret = getVal(TenantConfigEnum.OPEN_AUTH_MICROSOFT_CLIENT_SECRET);
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            log.debug("Microsoft 开放认证配置不完整，未启用");
            return Optional.empty();
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();
        return Optional.of(new AuthMicrosoftRequest(config, authStateCache));
    }

    private Optional<AuthRequest> buildWeChatRequest(String redirectUri) {
        String clientId = getVal(TenantConfigEnum.OPEN_AUTH_WECHAT_CLIENT_ID);
        String clientSecret = getVal(TenantConfigEnum.OPEN_AUTH_WECHAT_CLIENT_SECRET);
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientSecret)) {
            log.debug("微信开放认证配置不完整，未启用");
            return Optional.empty();
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();
        return Optional.of(new AuthWeChatOpenRequest(config, authStateCache));
    }

    private String getVal(TenantConfigEnum configKey) {
        return ktConfigService.getVal(configKey);
    }
}
