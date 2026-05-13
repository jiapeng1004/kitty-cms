package icu.jiapeng.kitty.user.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import icu.jiapeng.kitty.user.api.role.api.RoleMenuPermissionApi;
import icu.jiapeng.kitty.user.api.role.dto.GrantMenuRequestDTO;
import icu.jiapeng.kitty.user.role.service.RoleMenuPermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色菜单及权限开通 Controller
 */
@Tag(name = "角色菜单及权限 API")
@RestController
@Validated
public class KtRoleMenuPermissionController implements RoleMenuPermissionApi {

    @Resource
    private RoleMenuPermissionService roleMenuPermissionService;

    @Override
    @SaCheckPermission(KtPermissionCode.ROLE_UPDATE)
    public void grantMenuWithPermissionsSuper(@NotBlank(message = "role.id.not.blank") String roleId,
                                              @NotBlank(message = "menu.id.not.blank") String menuId) {
        roleMenuPermissionService.grantMenuWithPermissionsSuper(roleId, menuId);
    }

    @Override
    @SaCheckPermission(KtPermissionCode.ROLE_UPDATE)
    public void grantMenuWithPermissions(@Valid @RequestBody GrantMenuRequestDTO request) {
        roleMenuPermissionService.grantMenuWithPermissions(request.getRoleId(),
                request.getMenuId(),
                request.getPCodes());
    }
}

