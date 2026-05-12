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
package icu.jiapeng.kitty.plugin.sa.openfeign;

import cn.dev33.satoken.stp.StpInterface;
import icu.jiapeng.kitty.user.internal.api.UserInternalPermissionApi;
import icu.jiapeng.kitty.user.internal.api.UserInternalRoleApi;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 角色/权限列表经 OpenFeign 调用 kitty-user 内部 REST。
 */
@AllArgsConstructor
public class SaOpenFeignStpInterface implements StpInterface {

    private final UserInternalRoleApi userInternalRoleApi;
    private final UserInternalPermissionApi userInternalPermissionApi;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = userInternalPermissionApi.permissionCodesByUserId(loginId.toString());
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = userInternalRoleApi.roleCodesByUserId(loginId.toString());
        return list == null ? Collections.emptyList() : list;
    }
}
