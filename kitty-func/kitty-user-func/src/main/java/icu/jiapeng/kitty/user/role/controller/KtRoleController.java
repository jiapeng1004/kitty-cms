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
package icu.jiapeng.kitty.user.role.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import icu.jiapeng.kitty.user.role.entity.KtRole;
import icu.jiapeng.kitty.user.role.service.KtUserRoleService;
import icu.jiapeng.kitty.user.role.vo.KtRoleListInfoVo;
import icu.jiapeng.kitty.user.role.vo.RoleDetailInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色控制器
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@RestController
@Tag(name = "角色 API")
@Validated
public class KtRoleController {
    @Resource
    private KtUserRoleService ktUserRoleService;

    @ApiResponse(description = "角色详情")
    @Operation(summary = "角色id 获取获取角色详情")
    @SaCheckPermission(KtPermissionCode.ROLE_VIEW)
    public RoleDetailInfoVo detail(@NotBlank(message = "role.id.not.blank") @Schema(description = "角色id") @PathVariable String roleId) {
        return ktUserRoleService.detail(roleId);
    }

    @ApiResponse(description = "角色分页查询结果")
    @Operation(summary = "角色分页查询")
    @SaCheckPermission(KtPermissionCode.ROLE_VIEW)
    public KtRoleListInfoVo page() {
        KtRoleListInfoVo vo = new KtRoleListInfoVo();
        vo.setRecords(ktUserRoleService.lambdaQuery()
                .orderByDesc(KtRole::getCreateTime)
                .list()
                .stream()
                .map(role -> {
                    KtRoleListInfoVo.Item item = new KtRoleListInfoVo.Item();
                    item.setId(role.getId());
                    item.setRoleName(role.getRoleName());
                    item.setCreateTime(role.getCreateTime() != null ? role.getCreateTime().toString() : null);
                    return item;
                })
                .toList());
        return vo;
    }
}
