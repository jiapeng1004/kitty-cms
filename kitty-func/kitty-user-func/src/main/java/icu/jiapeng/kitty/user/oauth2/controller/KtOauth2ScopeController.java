package icu.jiapeng.kitty.user.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.user.api.oauth2.api.KtOauth2ScopeApi;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ScopeService;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ScopeVO;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * OAuth2 授权范围 API
 * <p>
 * 管理后台：scope 列表。
 */
@Tag(name = "OAuth2 授权范围 API", description = "管理后台：OAuth2 scope 列表。")
@RestController
@RequiredArgsConstructor
public class KtOauth2ScopeController implements KtOauth2ScopeApi {

    private final KtOauth2ScopeService ktOauth2ScopeService;

    /**
     * 获取 OAuth2 scope 列表
     */
    @Override
    @Operation(summary = "获取 OAuth2 scope 列表")
    @SaCheckPermission(KtPermissionCode.OAUTH2_SCOPE_VIEW)
    public List<Oauth2ScopeVO> list() {
        return ktOauth2ScopeService.listAll();
    }
}

