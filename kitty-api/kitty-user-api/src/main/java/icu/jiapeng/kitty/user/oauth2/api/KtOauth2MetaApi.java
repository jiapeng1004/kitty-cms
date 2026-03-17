package icu.jiapeng.kitty.user.oauth2.api;

import icu.jiapeng.kitty.user.oauth2.vo.Oauth2GrantTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@Tag(name = "开放 OAuth2 元信息 API")
public interface KtOauth2MetaApi {

    @Operation(summary = "获取 OAuth2 授权类型枚举列表")
    @GetExchange("/open/oauth2/grant-types")
    List<Oauth2GrantTypeVO> grantTypes();
}