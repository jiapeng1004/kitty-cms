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
package icu.jiapeng.kitty.user.dashboard.controller;

import icu.jiapeng.kitty.user.cfg.service.KtConfigClassService;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.api.dashboard.api.DashboardApi;
import icu.jiapeng.kitty.user.api.dashboard.vo.DashboardStatsVO;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘 API 实现，契约见 {@link DashboardApi}（kitty-user-api）
 *
 * @author jiapeng
 */
@Tag(name = "仪表盘 API")
@RestController
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {

    private final KtUserService ktUserService;
    private final KtConfigService ktConfigService;
    private final KtConfigClassService ktConfigClassService;

    @Override
    @Operation(summary = "统计概览")
    @SaCheckPermission(KtPermissionCode.SYSTEM_DASHBOARD_VIEW)
    public DashboardStatsVO stats() {
        return DashboardStatsVO.builder()
                .userCount(ktUserService.count())
                .configCount(ktConfigService.count())
                .configClassCount(ktConfigClassService.count())
                .build();
    }
}
