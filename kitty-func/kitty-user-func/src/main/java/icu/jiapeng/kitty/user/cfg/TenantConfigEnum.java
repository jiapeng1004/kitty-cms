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
package icu.jiapeng.kitty.user.cfg;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 租户/系统配置项枚举，与 kt_config 表 config_key 对应。
 * 获取配置应优先通过 {@link icu.jiapeng.kitty.user.cfg.service.KtConfigService#getVal(icu.jiapeng.kitty.user.cfg.TenantConfigEnum)}。
 * 查不到配置时返回本枚举维护的默认值（不依赖 configService/库表），多数为空串。
 */
@Getter
@RequiredArgsConstructor
public enum TenantConfigEnum {

    // ---------- 验证码（config-class-captcha） ----------
    CAPTCHA_SERVICE_TYPE("user.captcha.service.type", "验证码服务实现类型，如 hutool_redis", "hutool_redis"),

    // ---------- 开放认证/三方登录（config-class-oauth2） ----------
    OPEN_AUTH_FEISHU_CLIENT_ID("oauth2.feishu.client_id", "飞书开放平台应用 App ID", ""),
    OPEN_AUTH_FEISHU_CLIENT_SECRET("oauth2.feishu.client_secret", "飞书开放平台应用 App Secret", ""),
    OPEN_AUTH_DINGTALK_CLIENT_ID("oauth2.dingtalk.client_id", "钉钉扫码登录应用 API Key", ""),
    OPEN_AUTH_DINGTALK_CLIENT_SECRET("oauth2.dingtalk.client_secret", "钉钉扫码登录应用 Secret", ""),
    OPEN_AUTH_GITHUB_CLIENT_ID("oauth2.github.client_id", "GitHub OAuth App Client ID", ""),
    OPEN_AUTH_GITHUB_CLIENT_SECRET("oauth2.github.client_secret", "GitHub OAuth App Client Secret", ""),
    OPEN_AUTH_GOOGLE_CLIENT_ID("oauth2.google.client_id", "Google OAuth 2.0 Client ID", ""),
    OPEN_AUTH_GOOGLE_CLIENT_SECRET("oauth2.google.client_secret", "Google OAuth 2.0 Client Secret", ""),
    OPEN_AUTH_MICROSOFT_CLIENT_ID("oauth2.microsoft.client_id", "Microsoft Entra ID 应用(客户端) ID", ""),
    OPEN_AUTH_MICROSOFT_CLIENT_SECRET("oauth2.microsoft.client_secret", "Microsoft 客户端密码", ""),
    OPEN_AUTH_WECHAT_CLIENT_ID("oauth2.wechat.client_id", "微信开放平台网站应用 AppID", ""),
    OPEN_AUTH_WECHAT_CLIENT_SECRET("oauth2.wechat.client_secret", "微信开放平台网站应用 AppSecret", ""),
    OPEN_AUTH_REDIRECT_AFTER_LOGIN("oauth2.redirect_after_login", "第三方登录成功后跳转的前端地址，为空则相对路径 /login", ""),
    ;

    private final String key;
    private final String desc;
    /** 查不到配置时使用的默认值，不依赖 kt_config 表 */
    private final String defaultVal;
}
