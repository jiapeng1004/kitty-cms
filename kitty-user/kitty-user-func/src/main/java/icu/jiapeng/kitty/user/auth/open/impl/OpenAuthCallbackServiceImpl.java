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

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.user.auth.open.OpenAuthCallbackService;
import icu.jiapeng.kitty.user.auth.open.service.KtOpenUserService;
import icu.jiapeng.kitty.user.user.entity.KtUserUser;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import icu.jiapeng.kitty.user.user.vo.LoginResultVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * 开放认证回调默认实现：根据第三方用户查找或创建本地用户并执行 Sa-Token 登录。
 * 上游 SSO 与本地用户通过 kt_open_user 表映射（source + open_uid -> user_id），便于扩展 open 用户属性，性能与安全性更好。
 */
@Slf4j
@Service
public class OpenAuthCallbackServiceImpl implements OpenAuthCallbackService {

    @Resource
    private KtOpenUserService ktOpenUserService;
    @Resource
    private KtUserService ktUserService;

    @Override
    public Optional<LoginResultVo> loginByOAuthUser(String source, String uuid, String nickname, String username) {
        if (StrUtil.isBlank(uuid)) {
            return Optional.empty();
        }
        String src = source.trim().toLowerCase();

        Optional<String> userIdOpt = ktOpenUserService.findUserIdBySourceAndOpenUid(src, uuid);
        String userId;
        if (userIdOpt.isPresent()) {
            userId = userIdOpt.get();
        } else {
            userId = createUserAndBindOpenUser(src, uuid, nickname, username);
            if (userId == null) {
                return Optional.empty();
            }
        }

        StpUtil.login(userId);
        SaSession tokenSession = StpUtil.getTokenSession();
        long timeout = tokenSession.getTimeout();
        String token = tokenSession.getToken();
        LoginResultVo vo = new LoginResultVo();
        vo.setToken(token);
        vo.setTimeout(timeout);
        return Optional.of(vo);
    }

    private String createUserAndBindOpenUser(String source, String openUid, String nickname, String username) {
        String nick = StrUtil.isNotBlank(nickname) ? nickname : username;
        if (StrUtil.isBlank(nick)) {
            nick = "open_" + openUid;
        }
        KtUserUser user = new KtUserUser();
        user.setId(UUID.randomUUID().toString());
        user.setNickName(nick);
        user.setRealName(StrUtil.isNotBlank(nickname) ? nickname : null);
        user.setPwd(BCrypt.hashpw(UUID.randomUUID().toString()));
        user.setStatus(icu.jiapeng.kitty.user.constans.UserStatus.NORMAL.getStatus());
        if (!ktUserService.save(user)) {
            return null;
        }
        if (!ktOpenUserService.bind(source, openUid, user.getId(), null)) {
            log.warn("开放用户绑定失败: source={}, openUid={}", source, openUid);
            return null;
        }
        return user.getId();
    }
}
