package icu.jiapeng.kitty.material.user;

import java.util.List;

/**
 * 用户上下文网关（领域端口）。
 */
public interface UserContextGateway {

    /**
     * 当前登录用户ID。
     */
    String currentUserId();

    /**
     * 当前登录用户角色ID集合。
     */
    List<String> currentRoleIds();

    /**
     * 当前登录用户权限编码集合（与 sa-token StpInterface、kitty-user gRPC 插件返回的权限列表一致）。
     */
    List<String> currentPermissionCodes();

    /**
     * 指定用户的角色
     */
    List<String> userRoleIds(String userId);
}
