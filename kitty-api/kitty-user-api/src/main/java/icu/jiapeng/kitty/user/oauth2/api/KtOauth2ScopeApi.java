package icu.jiapeng.kitty.user.oauth2.api;

import icu.jiapeng.kitty.user.oauth2.vo.Oauth2ScopeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

/**
 * OAuth2 授权范围 API
 * <p>
 * 管理后台：scope 列表。
 */
@Tag(name = "OAuth2 授权范围 API", description = "管理后台：OAuth2 scope 列表。")
public interface KtOauth2ScopeApi {

    /**
     * 获取 OAuth2 scope 列表
     */
    @Operation(summary = "获取 OAuth2 scope 列表")
    @GetExchange("/api/oauth2-scope/list")
    List<Oauth2ScopeVO> list();
}
