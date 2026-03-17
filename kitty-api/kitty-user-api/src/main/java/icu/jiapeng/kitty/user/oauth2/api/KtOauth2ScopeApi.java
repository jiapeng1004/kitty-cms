package icu.jiapeng.kitty.user.oauth2.api;

import icu.jiapeng.kitty.user.oauth2.vo.Oauth2ScopeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@Tag(name = "OAuth2 Scope API")
public interface KtOauth2ScopeApi {

    @Operation(summary = "获取 OAuth2 scope 列表")
    @GetExchange("/api/oauth2-scope/list")
    List<Oauth2ScopeVO> list();
}
