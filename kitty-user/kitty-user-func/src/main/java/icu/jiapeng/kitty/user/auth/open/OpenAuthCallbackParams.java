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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 开放认证回调参数，与具体实现（如 JustAuth AuthCallback）解耦。
 * 用于 Controller 回调接口的请求参数封装（GET query 绑定 code、state），并传入 {@link OpenAuthService#handleCallback}。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAuthCallbackParams {

    /**
     * 授权码
     */
    private String code;
    /**
     * 防 CSRF 的 state
     */
    private String state;
    /**
     * 场景标识（如 admin），与 render 的 next 一致；回调命中 /callback/{source}/{next} 时由 Controller 从路径注入，供实现用相同 redirect_uri 换 token。
     */
    private String next;
}
