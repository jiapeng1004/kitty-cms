package icu.jiapeng.kitty.oauth2.server.springweb.web;

import icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerApi;
import icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerService;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2AuthorizeRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenIntrospectionRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRevocationRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2AuthorizationServerMetadataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * OAuth2 授权服务器
 * <p>
 * MVC 入口，实现 {@link OAuth2AuthorizationServerApi}。仅做参数绑定与委托；{@code body == null} 时补空 DTO 为防御性写法。
 *
 * @see icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerApi
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 */
@Tag(
        name = "OAuth2 授权服务器",
        description = "RFC 6749/6750/7636/7009/7662/8414 约定的授权服务器 HTTP 端点（令牌、授权重定向、自省、撤销、元数据）。")
@RestController
@RequiredArgsConstructor
public class OAuth2AuthorizationServerController implements OAuth2AuthorizationServerApi {

    private final OAuth2AuthorizationServerService oauth2AuthorizationServerService;

    /**
     * 令牌端点
     * <p>
     * 见 {@link OAuth2AuthorizationServerApi#token(String, OAuth2TokenRequest)}。
     *
     * @see OAuth2AuthorizationServerApi#token(String, OAuth2TokenRequest)
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
     */
    @Override
    @Operation(
            summary = "令牌端点",
            description = "POST /oauth2/token，RFC 6749 §3.2，application/x-www-form-urlencoded。")
    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @ModelAttribute OAuth2TokenRequest body) {
        if (body == null) {
            body = new OAuth2TokenRequest();
        }
        return oauth2AuthorizationServerService.processTokenRequest(authorization, body);
    }

    /**
     * 令牌自省端点
     * <p>
     * 见 {@link OAuth2AuthorizationServerApi#introspect(String, OAuth2TokenIntrospectionRequest)}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
     */
    @Override
    @Operation(
            summary = "令牌自省端点",
            description = "POST /oauth2/introspect，RFC 7662，application/x-www-form-urlencoded。")
    @PostMapping(value = "/oauth2/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> introspect(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @ModelAttribute OAuth2TokenIntrospectionRequest body) {
        if (body == null) {
            body = new OAuth2TokenIntrospectionRequest();
        }
        return oauth2AuthorizationServerService.processTokenIntrospection(authorization, body);
    }

    /**
     * 令牌撤销端点
     * <p>
     * 见 {@link OAuth2AuthorizationServerApi#revoke(String, OAuth2TokenRevocationRequest)}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
     */
    @Override
    @Operation(
            summary = "令牌撤销端点",
            description = "POST /oauth2/revoke，RFC 7009，application/x-www-form-urlencoded。")
    @PostMapping(value = "/oauth2/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> revoke(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @ModelAttribute OAuth2TokenRevocationRequest body) {
        if (body == null) {
            body = new OAuth2TokenRevocationRequest();
        }
        return oauth2AuthorizationServerService.processTokenRevocation(authorization, body);
    }

    /**
     * 授权端点
     * <p>
     * 见 {@link OAuth2AuthorizationServerApi#authorize(OAuth2AuthorizeRequest, HttpServletRequest, HttpServletResponse)}。
     *
     * @see OAuth2AuthorizationServerApi#authorize(OAuth2AuthorizeRequest, HttpServletRequest, HttpServletResponse)
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
     */
    @Override
    @Operation(
            summary = "授权端点",
            description = "GET /oauth2/authorize，RFC 6749 §4.1.1 授权码模式。")
    @GetMapping("/oauth2/authorize")
    public void authorize(
            @ModelAttribute OAuth2AuthorizeRequest authorizeRequest,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        oauth2AuthorizationServerService.processAuthorizationRequest(authorizeRequest, request, response);
    }

    /**
     * 授权服务器元数据
     * <p>
     * 见 {@link OAuth2AuthorizationServerApi#authorizationServerMetadata(HttpServletRequest)}。
     *
     * @see OAuth2AuthorizationServerApi#authorizationServerMetadata(HttpServletRequest)
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
     */
    @Override
    @Operation(
            summary = "授权服务器元数据",
            description = "GET /.well-known/oauth-authorization-server，RFC 8414。")
    @GetMapping(value = "/.well-known/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2AuthorizationServerMetadataResponse authorizationServerMetadata(HttpServletRequest request) {
        return oauth2AuthorizationServerService.buildAuthorizationServerMetadata(request);
    }
}
