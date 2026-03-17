package icu.jiapeng.kitty.user.role.service;

import java.util.List;

/**
 * 角色开通菜单及权限相关服务
 */
public interface RoleMenuPermissionService {

    /**
     * Super 接口：为角色开通菜单，并按照菜单声明的全部 p_codes 自动开通权限
     *
     * @param roleId  角色 id
     * @param menuId  菜单 id
     */
    void grantMenuWithPermissionsSuper(String roleId, String menuId);

    /**
     * 主接口：为角色开通菜单，并按前端回传的权限 code 列表开通权限
     *
     * @param roleId        角色 id
     * @param menuId        菜单 id
     * @param selectedPCodes 前端选择的权限 code 列表
     */
    void grantMenuWithPermissions(String roleId, String menuId, List<String> selectedPCodes);
}

