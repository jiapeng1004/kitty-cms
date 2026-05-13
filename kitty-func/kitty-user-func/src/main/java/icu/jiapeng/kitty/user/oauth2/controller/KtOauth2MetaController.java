package icu.jiapeng.kitty.user.oauth2.controller;

import icu.jiapeng.kitty.user.api.oauth2.api.KtOauth2MetaApi;
import icu.jiapeng.kitty.user.oauth2.enums.Oauth2ClientAuthenticationMethod;
import icu.jiapeng.kitty.user.oauth2.enums.Oauth2GrantType;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ClientAuthenticationMethodVO;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2GrantTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 开放 OAuth2 元信息 API
 * <p>
 * 无需登录：授权类型、客户端认证方式等枚举，供前端表单选项。
 */
@Tag(name = "开放 OAuth2 元信息 API", description = "开放接口：OAuth2 枚举元数据（授权类型、客户端认证方式）。")
@RestController
public class KtOauth2MetaController implements KtOauth2MetaApi {

    /**
     * 授权类型枚举
     */
    @Override
    @Operation(
            summary = "授权类型枚举",
            description = "GET /open/oauth2/grant-types，返回授权类型 code/desc。")
    public List<Oauth2GrantTypeVO> grantTypes() {
        return Arrays.stream(Oauth2GrantType.values())
                .map(item -> {
                    Oauth2GrantTypeVO vo = new Oauth2GrantTypeVO();
                    vo.setCode(item.getCode());
                    vo.setDesc(item.getDesc());
                    return vo;
                })
                .toList();
    }

    /**
     * 客户端认证方式枚举
     */
    @Override
    @Operation(
            summary = "客户端认证方式枚举",
            description = "GET /open/oauth2/client-authentication-methods，返回认证方式 code/desc。")
    public List<Oauth2ClientAuthenticationMethodVO> clientAuthenticationMethods() {
        return Arrays.stream(Oauth2ClientAuthenticationMethod.values())
                .map(item -> {
                    Oauth2ClientAuthenticationMethodVO vo = new Oauth2ClientAuthenticationMethodVO();
                    vo.setCode(item.getCode());
                    vo.setDesc(item.getDesc());
                    return vo;
                })
                .toList();
    }
}
