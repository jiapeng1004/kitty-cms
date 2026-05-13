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
package icu.jiapeng.kitty.user.controller.internal;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.user.api.internal.api.UserInternalAuthApi;
import icu.jiapeng.kitty.user.api.internal.dto.*;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInternalAuthController implements UserInternalAuthApi {

    @Resource
    @Lazy
    private SaTokenDao saTokenDao;

    @Override
    public TokenIntrospectionPayload tokenIntrospection(String token) {
        try {
            Object loginIdByToken = StpUtil.getLoginIdByToken(token);
            return new TokenIntrospectionPayload(loginIdByToken != null);
        } catch (Exception e) {
            return new TokenIntrospectionPayload(false);
        }
    }

    @Override
    public SaTokenValuePayload saTokenDaoGet(String key) {
        String value = saTokenDao.get(key);
        if (StrUtil.isBlank(value)) {
            value = "";
        }
        return new SaTokenValuePayload(value);
    }

    @Override
    public SaTokenTimeoutPayload saTokenDaoTimeout(String key) {
        return new SaTokenTimeoutPayload(saTokenDao.getTimeout(key));
    }

    @Override
    public SaTokenDaoUpdatePayload saTokenDaoUpdate(SaTokenDaoUpdateBody body) {
        try {
            saTokenDao.update(body.key(), body.value());
            return new SaTokenDaoUpdatePayload(true);
        } catch (Exception e) {
            return new SaTokenDaoUpdatePayload(false);
        }
    }
}
