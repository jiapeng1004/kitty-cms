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

import icu.jiapeng.kitty.user.user.vo.LoginResultVo;

import java.util.Optional;

/**
 * 开放认证回调服务：根据第三方用户标识查找或创建本地用户并完成登录，返回 token。
 * 由 {@link OpenAuthService} 实现类在 handleCallback 内部调用，不暴露给 Controller。
 */
public interface OpenAuthCallbackService {

    /**
     * 根据第三方用户标识查找或创建用户并登录，返回登录结果。
     *
     * @param source   平台标识：feishu、dingtalk、github 等
     * @param uuid     第三方用户唯一标识
     * @param nickname 昵称（新建用户时使用）
     * @param username 用户名（新建用户时使用，可为空）
     * @return 登录结果（token、timeout），失败返回 empty
     */
    Optional<LoginResultVo> loginByOAuthUser(String source, String uuid, String nickname, String username);
}
