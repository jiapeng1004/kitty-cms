package icu.jiapeng.kitty.user.role.api;

import icu.jiapeng.kitty.user.role.dto.GrantMenuRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 角色菜单及权限开通 API
 */
@Tag(name = "角色菜单及权限 API")
public interface RoleMenuPermissionApi {

    @Operation(summary = "Super接口：按菜单全部p_codes为角色开通菜单及权限")
    @PostExchange("/api/admin/role/menu/super/grant")
    void grantMenuWithPermissionsSuper(@NotBlank(message = "role.id.not.blank") String roleId,
                                       @NotBlank(message = "menu.id.not.blank") String menuId);

    @Operation(summary = "主接口：按前端勾选的权限code为角色开通菜单及权限")
    @PostExchange("/api/role/menu/grant")
    void grantMenuWithPermissions(@Valid GrantMenuRequestDTO request);
}

