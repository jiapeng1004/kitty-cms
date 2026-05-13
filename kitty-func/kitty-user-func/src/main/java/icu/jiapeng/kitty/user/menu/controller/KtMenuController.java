package icu.jiapeng.kitty.user.menu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import icu.jiapeng.kitty.user.api.menu.api.KtMenuApi;
import icu.jiapeng.kitty.user.menu.service.KtMenuService;
import icu.jiapeng.kitty.user.api.menu.vo.MenuTreeVo;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import icu.jiapeng.kitty.user.permission.service.KtPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@Tag(name = "菜单 API")
public class KtMenuController implements KtMenuApi {

    @Resource
    private KtMenuService ktMenuService;
    @Resource
    private KtPermissionService ktPermissionService;

    /**
     * 菜单管理：查看完整菜单树（不做权限过滤）
     */
    @Operation(summary = "获取完整菜单树")
    @SaCheckPermission(KtPermissionCode.MENU_VIEW)
    @Override
    public List<MenuTreeVo> allTree() {
        return ktMenuService.getFullMenuTree();
    }

    /**
     * 当前登录用户可见菜单树：基于其拥有的权限 code 过滤
     */
    @Operation(summary = "获取当前用户可见菜单树")
    @Override
    public List<MenuTreeVo> currentUserTree() {
        String userId = Objects.requireNonNull(StpUtil.getLoginId(), "当前用户未登录").toString();
        List<String> codes = ktPermissionService.getUserPermissionCodeList(userId);
        return ktMenuService.getMenuTreeByPermissions(codes);
    }
}

