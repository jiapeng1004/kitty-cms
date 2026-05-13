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
package icu.jiapeng.kitty.user.api.open;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 开放认证「渲染」请求参数：用于 /render 接口的 query 绑定。
 * next 会拼接到发给第三方的 redirect_uri 后，以便回调时命中 /callback/{source}/{next}。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAuthRenderParams {

    /**
     * 登录后要去的场景标识，如 admin、app；会拼到 redirect_uri 路径末尾，回调时从路径取到 next 再查配置跳转。
     */
    private String next;

    /**
     * 不直接redirect 过去
     */
    private Boolean noRedirect;
}
