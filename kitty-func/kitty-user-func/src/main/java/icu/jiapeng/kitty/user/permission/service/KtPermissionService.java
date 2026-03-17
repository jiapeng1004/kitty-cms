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
package icu.jiapeng.kitty.user.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.user.permission.entity.KtPermission;

import java.util.List;

/**
 * 权限服务
 *
 * @author jiapeng
 * @since 2025/12/19
 */
public interface KtPermissionService extends IService<KtPermission> {
    /**
     * 获取用户拥有的权限
     *
     * @param userId 登录id
     * @return 用户权限
     */
    List<String> getUserPermissionCodeList(String userId);


    /**
     * 根据角色id查询权限code
     * @param roles 角色id集合
     * @return 权限code集合
     */
    List<String> getPermissionCodesByRoleIds(List<String> roles);

    /**
     * 指定API是否需要授权
     * @param permissionCode 权限
     * @return true:拥有权限,false:没有权限
     */
    Boolean requirePermission(String permissionCode);
}
