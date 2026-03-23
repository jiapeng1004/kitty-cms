package icu.jiapeng.kitty.oauth2.server.springweb.api;

import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2AuthorizeRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenIntrospectionRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRevocationRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2AuthorizationServerMetadataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.io.IOException;

/**
 * OAuth2 授权服务器
 * <p>
 * Spring {@code HttpExchange} 契约，供 OpenAPI 与声明式客户端对齐。各方法规范细节见 {@code @Operation#description} 与本接口方法注释下文。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@Tag(
        name = "OAuth2 授权服务器",
        description = "RFC 6749/6750/7636/7009/7662/8414 约定的授权服务器 HTTP 端点（令牌、授权重定向、自省、撤销、元数据）。")
public interface OAuth2AuthorizationServerApi {

    /**
     * 令牌端点
     * <p>
     * 【规范】RFC 6749 §3.2：{@code POST}，正文 {@code application/x-www-form-urlencoded}；成功/错误 JSON 见 §5.1 / §5.2；
     * 客户端认证见 §2.3（{@code Authorization: Basic} 与 {@code client_id}/{@code client_secret} 等）。Bearer 见 RFC 6750。
     * <p>
     * 【本模块】路径 {@code /oauth2/token}；请求体 {@link OAuth2TokenRequest}；响应见 {@code OAuth2TokenSuccessResponse} / {@code OAuth2ErrorResponse}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-3.2">RFC 6749 §3.2 Token Endpoint</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.1">RFC 6749 §5.1 Successful Response</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.2">RFC 6749 §5.2 Error Response</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-2.3">RFC 6749 §2.3 Client Authentication</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750 Bearer Token Usage</a>
     */
    @Operation(
            summary = "令牌端点",
            description = "POST /oauth2/token，RFC 6749 §3.2，application/x-www-form-urlencoded。")
    @PostExchange(value = "/oauth2/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<?> token(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @ModelAttribute OAuth2TokenRequest body);

    /**
     * 令牌自省端点
     * <p>
     * 【规范】RFC 7662：{@code POST}，{@code application/x-www-form-urlencoded}；客户端认证同令牌端点（§2.3）。
     * <p>
     * 【本模块】路径 {@code /oauth2/introspect}；请求体 {@link OAuth2TokenIntrospectionRequest}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
     */
    @Operation(
            summary = "令牌自省端点",
            description = "POST /oauth2/introspect，RFC 7662，application/x-www-form-urlencoded。")
    @PostExchange(value = "/oauth2/introspect", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<?> introspect(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @ModelAttribute OAuth2TokenIntrospectionRequest body);

    /**
     * 令牌撤销端点
     * <p>
     * 【规范】RFC 7009：{@code POST}，{@code application/x-www-form-urlencoded}；客户端认证同令牌端点（§2.3）。
     * 成功响应为 HTTP 200，响应体为空。
     * <p>
     * 【本模块】路径 {@code /oauth2/revoke}；请求体 {@link OAuth2TokenRevocationRequest}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
     */
    @Operation(
            summary = "令牌撤销端点",
            description = "POST /oauth2/revoke，RFC 7009，application/x-www-form-urlencoded。")
    @PostExchange(value = "/oauth2/revoke", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<?> revoke(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @ModelAttribute OAuth2TokenRevocationRequest body);

    /**
     * 授权端点
     * <p>
     * 【规范】RFC 6749 §4.1.1 授权码模式第一步：{@code GET}，查询含 {@code response_type}、{@code client_id}、{@code redirect_uri}、{@code scope}、{@code state}；PKCE 见 RFC 7636。
     * 成功为重定向带 {@code code}（§4.1.2）；可重定向错误见 §4.1.2.1。
     * <p>
     * 【本模块】路径 {@code /oauth2/authorize}；参数 {@link OAuth2AuthorizeRequest}；另传 {@link HttpServletRequest}/{@link HttpServletResponse} 用于登录跳转等 SPI，非 RFC 规定形参表。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.1.1">RFC 6749 §4.1.1 Authorization Endpoint</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.1.2">RFC 6749 §4.1.2 Authorization Response</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.1.2.1">RFC 6749 §4.1.2.1 Error Response</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636 PKCE</a>
     */
    @Operation(
            summary = "授权端点",
            description = "GET /oauth2/authorize，RFC 6749 §4.1.1 授权码模式。")
    @GetExchange("/oauth2/authorize")
    void authorize(
            @ModelAttribute OAuth2AuthorizeRequest authorizeRequest,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException;

    /**
     * 授权服务器元数据
     * <p>
     * 【规范】RFC 8414：{@code GET /.well-known/oauth-authorization-server}，JSON 字段以该 RFC 为准。
     * <p>
     * 【本模块】{@link HttpServletRequest} 用于未配置静态 {@code issuer} 时推导 issuer；返回 {@link OAuth2AuthorizationServerMetadataResponse}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414 Authorization Server Metadata</a>
     */
    @Operation(
            summary = "授权服务器元数据",
            description = "GET /.well-known/oauth-authorization-server，RFC 8414。")
    @GetExchange("/.well-known/oauth-authorization-server")
    OAuth2AuthorizationServerMetadataResponse authorizationServerMetadata(HttpServletRequest request);
}
