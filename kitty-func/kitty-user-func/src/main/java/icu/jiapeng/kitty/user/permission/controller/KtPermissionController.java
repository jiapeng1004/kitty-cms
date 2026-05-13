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
package icu.jiapeng.kitty.user.permission.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.user.api.permission.api.KtPermissionApi;
import icu.jiapeng.kitty.user.permission.service.KtPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 权限http
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@RestController
@Tag(name = "权限 api")
public class KtPermissionController implements KtPermissionApi {
    @Resource
    private KtPermissionService ktPermissionService;

    /**
     * 获取用户的权限列表,需要用户有user:permission:aware权限
     *
     * @return 权限列表
     */
    @ApiResponse(description = "权限列表")
    @Operation(summary = "获取用户的权限列表")
    public List<String> list(
            @Schema(description = "用户id,不传递的时候为当前用户", example = "1")
            @RequestParam(required = false) String userId) {
        if (StrUtil.isBlank(userId)) {
            userId = Objects.requireNonNull(StpUtil.getLoginId(), "当前用户未登录").toString();
        }
        return ktPermissionService.getUserPermissionCodeList(userId);
    }
}
