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
package icu.jiapeng.kitty.user.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.user.role.convert.BeansConvert;
import icu.jiapeng.kitty.user.role.entity.KtRole;
import icu.jiapeng.kitty.user.role.entity.KtUserRole;
import icu.jiapeng.kitty.user.role.mapper.KtUserRoleMapper;
import icu.jiapeng.kitty.user.role.service.KtUserRoleService;
import icu.jiapeng.kitty.user.role.service.KtUserRoleUserService;
import icu.jiapeng.kitty.user.api.role.vo.RoleDetailInfoVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class KtUserRoleServiceImpl extends ServiceImpl<KtUserRoleMapper, KtRole> implements KtUserRoleService {
    @Resource
    private KtUserRoleUserService ktUserRoleUserService;

    @Override
    public List<String> getRoleIdsByUserId(String string) {
        // 获取用户的角色
        return ktUserRoleUserService.lambdaQuery().select(KtUserRole::getRoleId).eq(KtUserRole::getUserId, string)
                .list().stream().map(KtUserRole::getRoleId).toList();
    }

    @Override
    public RoleDetailInfoVo detail(String roleId) {
        KtRole role = getById(roleId);
        if (Objects.isNull(role)) {
            throw BizException.of(ResultStatus.ROLE_NOT_EXIST);
        }
        return BeansConvert.INSTANCE.role2Detail(role);
    }
}
