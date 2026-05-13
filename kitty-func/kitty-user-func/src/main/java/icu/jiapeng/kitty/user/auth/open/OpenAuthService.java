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

import icu.jiapeng.kitty.user.api.open.OpenAuthCallbackParams;
import icu.jiapeng.kitty.user.api.open.OpenAuthRenderParams;
import icu.jiapeng.kitty.user.api.user.vo.LoginResultVo;

import java.util.Optional;

/**
 * 开放认证（三方登录）服务：与具体实现（如 JustAuth）解耦，仅定义「获取授权地址」与「用回调参数完成登录」。
 * 实现类内部可自行使用 JustAuth 等，不向接口暴露任何第三方库类型。
 */
public interface OpenAuthService {

    /**
     * 预检：该 source 是否已配置并就绪，可供登录页先调此接口再决定是否展示或跳转 render。
     *
     * @param source 平台标识：feishu、dingtalk、github 等
     * @return true 表示就绪，可跳转授权页；false 表示未配置或不可用
     */
    boolean isSourceReady(String source);

    /**
     * 获取跳转至第三方授权页的 URL。
     * redirect_uri 固定为当前服务的 /open/auth/callback/{source}（可选 /{next}），由工具类 {@link icu.jiapeng.kitty.common.core.util.CommonServletUtil#getCurrentBaseUrl()} 与 path 拼接。
     *
     * @param source 平台标识：feishu、dingtalk、github、google、microsoft、wechat
     * @param params 渲染参数（如 next），由请求 query 绑定，可为 null
     * @return 授权页 URL，未配置或不可用时为空
     */
    Optional<String> getAuthorizeUrl(String source, OpenAuthRenderParams params);

    /**
     * 用回调参数（code、state 等）完成第三方登录，由实现内部完成换码、查/建用户、登录等，返回登录结果。
     * 换 token 时使用的 redirect_uri 与授权时一致，通过 {@link icu.jiapeng.kitty.common.core.util.CommonServletUtil#getCurrentBaseUrl()} 获取当前域并拼接 /open/auth/callback/{source}[/{next}]。
     *
     * @param source 平台标识
     * @param params 回调参数（由实现解析）
     * @return 登录结果（token 等），失败或未配置时为空
     */
    Optional<LoginResultVo> handleCallback(String source, OpenAuthCallbackParams params);
}
