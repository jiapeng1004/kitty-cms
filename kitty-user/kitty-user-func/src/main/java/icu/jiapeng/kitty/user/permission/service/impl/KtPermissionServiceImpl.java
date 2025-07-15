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
package icu.jiapeng.kitty.user.permission.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionType;
import icu.jiapeng.kitty.user.permission.constants.KtPrOwnerType;
import icu.jiapeng.kitty.user.permission.entity.KtPermission;
import icu.jiapeng.kitty.user.permission.entity.KtPermissionOwner;
import icu.jiapeng.kitty.user.permission.mapper.KtPermissionMapper;
import icu.jiapeng.kitty.user.permission.service.KtPermissionOwnerService;
import icu.jiapeng.kitty.user.permission.service.KtPermissionService;
import icu.jiapeng.kitty.user.role.service.KtUserRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 权限 svc
 *
 * @author jiapeng
 * @since 2025/12/19
 */
@Service
@Slf4j
public class KtPermissionServiceImpl extends ServiceImpl<KtPermissionMapper, KtPermission> implements KtPermissionService {
    @Resource
    private KtUserRoleService ktUserRoleService;
    @Resource
    private KtPermissionOwnerService ktPermissionOwnerService;

    @Override
    public List<String> getUserPermissionCodeList(String userId) {
        // 获取用户的角色
        List<String> roles = ktUserRoleService.getRoleIdsByUserId(userId);
        // 获取角色的权限集合
        List<String> permissionCodes = getPermissionCodesByRoleIds(roles);
        // 获取用户的所有权限
        List<String> userPermissionCodes = getPermissionCodesByUserIdSelf(userId);
        // 去重
        List<String> userAllPermissionCodes = Stream.concat(userPermissionCodes.stream(), permissionCodes.stream()).distinct().toList();
        if (log.isDebugEnabled()) {
            log.debug("用户 {} 的权限为 {}", userId, userAllPermissionCodes);
        }
        return userPermissionCodes;
    }

    @Override
    public List<String> getPermissionCodesByRoleIds(List<String> roles) {
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        // 获取角色的权限集合
        return ktPermissionOwnerService.lambdaQuery()
                .in(KtPermissionOwner::getOwnerId, roles)
                .eq(KtPermissionOwner::getOwnerType, KtPrOwnerType.ROLE.getOwner())
                .select(KtPermissionOwner::getPCode)
                .list()
                .stream()
                .map(KtPermissionOwner::getPCode)
                .toList();

    }

    @Override
    public List<String> getPermissionCodesByUserIdSelf(String userId) {
        // 获取用户自己的权限
        return ktPermissionOwnerService.lambdaQuery()
                .eq(KtPermissionOwner::getOwnerId, userId)
                .eq(KtPermissionOwner::getOwnerType, KtPrOwnerType.USER.getOwner())
                .select(KtPermissionOwner::getPCode)
                .list()
                .stream()
                .map(KtPermissionOwner::getPCode)
                .toList();
    }

    @Override
    public Boolean requirePermission(String permissionCode) {
        // 判断路径是否有权限记录
        KtPermission apiPermission = lambdaQuery().eq(KtPermission::getPCode, permissionCode)
                .last("LIMIT 1")
                .orderByDesc(KtPermission::getCreateTime)
                .one();
        return Objects.nonNull(apiPermission);
    }
}
