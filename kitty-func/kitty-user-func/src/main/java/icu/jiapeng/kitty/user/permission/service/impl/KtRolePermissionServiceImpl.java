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


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.user.permission.entity.KtRolePermission;
import icu.jiapeng.kitty.user.permission.mapper.KtPermissionOwnerMapper;
import icu.jiapeng.kitty.user.permission.service.KtRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限所有者服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Service
public class KtRolePermissionServiceImpl extends ServiceImpl<KtPermissionOwnerMapper, KtRolePermission> implements KtRolePermissionService {
}
