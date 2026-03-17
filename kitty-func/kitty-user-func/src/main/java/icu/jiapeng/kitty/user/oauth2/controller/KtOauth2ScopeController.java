package icu.jiapeng.kitty.user.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.user.oauth2.api.KtOauth2ScopeApi;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ScopeService;
import icu.jiapeng.kitty.user.oauth2.vo.Oauth2ScopeVO;
import icu.jiapeng.kitty.user.permission.constants.KtPermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class KtOauth2ScopeController implements KtOauth2ScopeApi {

    private final KtOauth2ScopeService ktOauth2ScopeService;

    @Override
    @GetMapping("/api/oauth2-scope/list")
    @SaCheckPermission(KtPermissionCode.OAUTH2_SCOPE_VIEW)
    public List<Oauth2ScopeVO> list() {
        return ktOauth2ScopeService.listAll();
    }
}

