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
package icu.jiapeng.kitty.user.permission.constants;

/**
 * 权限code
 *
 * @author jiapeng
 * @since 2025/12/20
 */
public interface KtPermissionCode {

    // ================== 系统级 ==================

    String SYSTEM_DASHBOARD_VIEW = "system:dashboard:view";
    String SYSTEM_LOG_VIEW = "system:log:view";

    // ================== 用户 ==================
    String USER_VIEW = "user:account:view";
    String USER_CREATE = "user:account:create";
    String USER_UPDATE = "user:account:update";
    String USER_DELETE = "user:account:delete";
    String USER_RESET_PASSWORD = "user:account:reset-password";

    // ================== 用户的角色 ==================
    String USER_ROLE_VIEW = "user:role:view";
    String USER_ROLE_UPDATE = "user:role:update";


    // ================== 配置相关 ==================
    // 配置查看
    String CONFIG_VIEW = "config:view";
    // 配置创建
    String CONFIG_CREATE = "config:create";
    // 配置更新(仅更新值)
    String CONFIG_SET = "config:set";
    // 配置编辑
    String CONFIG_UPDATE = "config:update";
    // 配置删除
    String CONFIG_DELETE = "config:delete";

    // ================== oauth2客户端管理 ==================
    String OAUTH2_CLIENT_VIEW = "oauth2:client:view";
    String OAUTH2_CLIENT_CREATE = "oauth2:client:create";
    String OAUTH2_CLIENT_UPDATE = "oauth2:client:update";
    String OAUTH2_CLIENT_DELETE = "oauth2:client:delete";

    // ================== oauth2 scope管理 ==================
    String OAUTH2_SCOPE_VIEW = "oauth2:scope:view";

    // ================== 菜单管理 ==================
    String MENU_VIEW = "menu:view";
    String MENU_CREATE = "menu:create";
    String MENU_UPDATE = "menu:update";
    String MENU_DELETE = "menu:delete";

    // ================== 权限管理 ==================
    String PERMISSION_VIEW = "permission:view";
    String PERMISSION_CREATE = "permission:create";
    String PERMISSION_UPDATE = "permission:update";
    String PERMISSION_DELETE = "permission:delete";

    // ================== 角色管理 ==================
    String ROLE_VIEW = "role:view";
    String ROLE_CREATE = "role:create";
    String ROLE_UPDATE = "role:update";
    String ROLE_DELETE = "role:delete";

    // ================== 角色权限管理 ==================
    String ROLE_PERMISSION_VIEW = "role:permission:view";
    String ROLE_PERMISSION_UPDATE = "role:permission:update";


    //
}