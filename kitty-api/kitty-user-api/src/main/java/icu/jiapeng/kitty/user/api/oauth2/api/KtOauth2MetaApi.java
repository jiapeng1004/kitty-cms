package icu.jiapeng.kitty.user.api.oauth2.api;

import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ClientAuthenticationMethodVO;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2GrantTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 开放 OAuth2 元信息 API
 * <p>
 * 无需登录：授权类型、客户端认证方式等枚举，供前端表单选项。
 */
@Tag(name = "开放 OAuth2 元信息 API", description = "开放接口：OAuth2 枚举元数据（授权类型、客户端认证方式）。")
public interface KtOauth2MetaApi {

    /**
     * 授权类型枚举
     */
    @Operation(
            summary = "授权类型枚举",
            description = "GET /open/oauth2/grant-types，返回授权类型 code/desc。")
    @GetMapping("/open/oauth2/grant-types")
    List<Oauth2GrantTypeVO> grantTypes();

    /**
     * 客户端认证方式枚举
     */
    @Operation(
            summary = "客户端认证方式枚举",
            description = "GET /open/oauth2/client-authentication-methods，返回认证方式 code/desc。")
    @GetMapping("/open/oauth2/client-authentication-methods")
    List<Oauth2ClientAuthenticationMethodVO> clientAuthenticationMethods();
}