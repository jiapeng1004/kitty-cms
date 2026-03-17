package icu.jiapeng.kitty.user.role.service.impl;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.user.menu.entity.KtMenu;
import icu.jiapeng.kitty.user.menu.service.KtMenuService;
import icu.jiapeng.kitty.user.permission.entity.KtRolePermission;
import icu.jiapeng.kitty.user.permission.service.KtRolePermissionService;
import icu.jiapeng.kitty.user.role.entity.KtRole;
import icu.jiapeng.kitty.user.role.entity.KtRoleMenu;
import icu.jiapeng.kitty.user.role.mapper.KtRoleMenuMapper;
import icu.jiapeng.kitty.user.role.service.KtUserRoleService;
import icu.jiapeng.kitty.user.role.service.RoleMenuPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色开通菜单及权限服务实现
 */
@Service
public class RoleMenuPermissionServiceImpl implements RoleMenuPermissionService {

    @Resource
    private KtUserRoleService ktUserRoleService;

    @Resource
    private KtMenuService ktMenuService;

    @Resource
    private KtRoleMenuMapper ktRoleMenuMapper;

    @Resource
    private KtRolePermissionService ktRolePermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantMenuWithPermissionsSuper(String roleId, String menuId) {
        KtRole role = ktUserRoleService.getById(roleId);
        if (role == null) {
            throw BizException.of(ResultStatus.ROLE_NOT_EXIST);
        }
        KtMenu menu = ktMenuService.getById(menuId);
        if (menu == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        Set<String> menuPCodes = parsePCodes(menu.getPCodes());
        // Super 接口：直接使用菜单声明的全部 p_codes
        upsertRoleMenu(roleId, menuId);
        grantPermissionsIfAbsent(roleId, menuPCodes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantMenuWithPermissions(String roleId, String menuId, List<String> selectedPCodes) {
        KtRole role = ktUserRoleService.getById(roleId);
        if (role == null) {
            throw BizException.of(ResultStatus.ROLE_NOT_EXIST);
        }
        KtMenu menu = ktMenuService.getById(menuId);
        if (menu == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        Set<String> menuPCodes = parsePCodes(menu.getPCodes());
        Set<String> selected = CollectionUtils.isEmpty(selectedPCodes)
                ? Collections.emptySet()
                : selectedPCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());

        // 校验：selected ⊆ menuPCodes
        if (!menuPCodes.containsAll(selected)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }

        upsertRoleMenu(roleId, menuId);
        grantPermissionsIfAbsent(roleId, selected);
    }

    private Set<String> parsePCodes(String pCodes) {
        if (!StringUtils.hasText(pCodes)) {
            return Collections.emptySet();
        }
        return Arrays.stream(pCodes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private void upsertRoleMenu(String roleId, String menuId) {
        // 简单实现：按 roleId + menuId 查一条，有则略过，无则插入
        KtRoleMenu query = new KtRoleMenu();
        query.setRoleId(roleId);
        query.setMenuId(menuId);
        KtRoleMenu existing = ktRoleMenuMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KtRoleMenu>()
                        .lambda()
                        .eq(KtRoleMenu::getRoleId, roleId)
                        .eq(KtRoleMenu::getMenuId, menuId)
        );
        if (existing == null) {
            ktRoleMenuMapper.insert(query);
        }
    }

    private void grantPermissionsIfAbsent(String roleId, Set<String> pCodes) {
        if (CollectionUtils.isEmpty(pCodes)) {
            return;
        }
        for (String code : pCodes) {
            KtRolePermission existing = ktRolePermissionService.lambdaQuery()
                    .eq(KtRolePermission::getRoleId, roleId)
                    .eq(KtRolePermission::getPCode, code)
                    .one();
            if (existing != null) {
                continue;
            }
            KtRolePermission entity = new KtRolePermission();
            entity.setRoleId(roleId);
            entity.setPCode(code);
            ktRolePermissionService.save(entity);
        }
    }
}

