package icu.jiapeng.kitty.oauth2.server.springweb.internal;

import icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerService;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2AuthorizeRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenIntrospectionRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRevocationRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2AuthorizationServerMetadataResponse;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2ErrorResponse;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2TokenSuccessResponse;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenIntrospectionResponse;
import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2AuthorizationCodeSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2ScopeStrings;
import icu.jiapeng.kitty.oauth2.server.springweb.model.*;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationServerPropertiesPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentUiPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2LoginNavigationPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RegisteredClientRegistryPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ResourceOwnerPasswordPort;
import icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2Pkce;
import icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2Rfc6749ErrorCodes;
import icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2Rfc7662TokenTypeHints;
import icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2StandardGrantType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * OAuth2 授权服务器核心实现：在「规范定义的行为」与「本模块可替换细节」之间分界如下。
 * <p><b>【规范】所覆盖的行为（实现须与之兼容）</b></p>
 * <ul>
 *   <li>RFC 6749：授权码、刷新、客户端凭证、资源所有者密码等 grant；令牌端点请求/响应与错误格式（§3.2、§5.1、§5.2）；
 *       授权端点重定向与 query 错误参数（§4.1.1、§4.1.2、§4.1.2.1）。</li>
 *   <li>RFC 6750：访问令牌类型 {@code Bearer}。</li>
 *   <li>RFC 7636：授权码交换时的 PKCE 校验（本模块通过 {@link OAuth2Pkce}）。</li>
 *   <li>RFC 8414：{@link #buildAuthorizationServerMetadata} 返回的元数据字段语义。</li>
 *   <li>RFC 7662：{@link #processTokenIntrospection} 自省端点对 {@code token} 的 active 判定与元数据字段。</li>
 *   <li>RFC 7009：{@link #processTokenRevocation} 撤销端点；成功时 HTTP 200 空体（无效或未知令牌亦同，避免信息泄露）。</li>
 * </ul>
 * <p><b>【本模块定制】（实现细节，可随版本调整但会单独标注）</b></p>
 * <ul>
 *   <li>授权码 TTL 见配置 {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationServerPropertiesPort#authorizationCodeTtlSeconds()}；
 *       令牌字节长度与 Base64URL 编码格式、access/refresh 默认 TTL 等其余常量见本类。</li>
 *   <li>无法在 {@code redirect_uri} 上安全返回错误时，使用 {@link HttpServletResponse#sendError(int, String)}，
 *       响应体非 OAuth JSON（与常见浏览器授权页一致；纯 API 客户端可另在网关扩展）。</li>
 *   <li>未登录浏览器会话时跳转登录页：经 {@link OAuth2LoginNavigationPort}，参数名 {@code oauth2_redirect} 等为
 *       <b>本仓库与宿主前端约定</b>，非 IETF OAuth 核心规范所定义。</li>
 *   <li>客户端与令牌持久化完全由 SPI 决定，本类不假设 DB/Redis 形态。</li>
 *   <li><b>授权码流程与 {@code redirect_uri}</b>：RFC 6749 §4.1.1 在客户端仅登记一个重定向 URI 时，允许授权请求<b>省略</b> {@code redirect_uri}；§4.1.3 规定若授权请求中带了 {@code redirect_uri}，换 token 时须带且一致。本模块在<b>授权端</b>与<b>Token 端（{@code grant_type=authorization_code}）</b>均<b>始终要求</b>显式传递 {@code redirect_uri}，<b>未实现</b>「单回调可省略」的 OPTIONAL 分支。此为相对 RFC 最低要求的<b>局部策略加严</b>（显式绑定、降低歧义与错误配置风险），并非 RFC 另有独立强制条文。对接客户端时请在 {@code /oauth2/authorize} 与换 token 表单中<b>始终</b>传递一致的 {@code redirect_uri}。</li>
 * </ul>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@RequiredArgsConstructor
public class OAuth2AuthorizationServerCoreService implements OAuth2AuthorizationServerService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OAuth2RegisteredClientRegistryPort clientRegistry;
    private final OAuth2AuthorizationCodePersistencePort authorizationCodePersistence;
    private final OAuth2AccessTokenPersistencePort accessTokenPersistence;
    private final OAuth2RefreshTokenPersistencePort refreshTokenPersistence;
    private final OAuth2ResourceOwnerPasswordPort resourceOwnerPassword;
    private final OAuth2EndUserSessionPort endUserSession;
    private final OAuth2LoginNavigationPort loginNavigation;
    private final OAuth2AuthorizationServerPropertiesPort properties;
    private final OAuth2ConsentStoragePort consentStorage;
    private final OAuth2ConsentUiPort consentUi;

    /**
     * 【本模块定制】从 Basic / 表单解析出的客户端凭据与所用认证方式标签。
     */
    private record ParsedClientCredentials(String clientId, String clientSecret, String authMethod) {
    }

    // -------------------------------------------------------------------------
    // 【规范】RFC 6749 §3.2 Token Endpoint
    // -------------------------------------------------------------------------

    /**
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-3.2">RFC 6749 §3.2</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
     */
    @Override
    public ResponseEntity<?> processTokenRequest(String authorizationHeader, OAuth2TokenRequest body) {
        OAuth2TokenRequest form = body != null ? body : new OAuth2TokenRequest();
        String grantType = form.getGrant_type();
        if (!StringUtils.hasText(grantType)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "grant_type is required");
        }
        ParsedClientCredentials creds = parseClientCredentials(authorizationHeader, form.getClient_id(), form.getClient_secret());
        return switch (grantType) {
            case OAuth2StandardGrantType.CLIENT_CREDENTIALS -> grantClientCredentials(creds, form);
            case OAuth2StandardGrantType.PASSWORD -> grantPassword(creds, form);
            case OAuth2StandardGrantType.REFRESH_TOKEN -> grantRefreshToken(creds, form);
            case OAuth2StandardGrantType.AUTHORIZATION_CODE -> grantAuthorizationCode(creds, form);
            default -> errorBody(OAuth2Rfc6749ErrorCodes.UNSUPPORTED_GRANT_TYPE, HttpStatus.BAD_REQUEST, null);
        };
    }

    /**
     * 【规范】RFC 7662 Token Introspection：调用方须为已通过 RFC 6749 §2.3 认证的机密客户端；成功响应为 JSON。
     * <p>
     * 【本模块定制】{@code exp}/{@code iat} 当前未填充（持久化端口未暴露绝对过期时刻）；活跃时令牌元数据来自 {@link OAuth2TokenSnapshot}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
     */
    @Override
    public ResponseEntity<?> processTokenIntrospection(String authorizationHeader, OAuth2TokenIntrospectionRequest body) {
        OAuth2TokenIntrospectionRequest form = body != null ? body : new OAuth2TokenIntrospectionRequest();
        ParsedClientCredentials creds = parseClientCredentials(authorizationHeader, form.getClient_id(), form.getClient_secret());
        OAuth2RegisteredClientSnapshot caller = loadAndValidateClient(creds.clientId(), creds.clientSecret(), creds.authMethod());
        if (caller == null) {
            return invalidClient();
        }
        String token = form.getToken();
        if (!StringUtils.hasText(token)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "token is required");
        }
        String hint = form.getToken_type_hint();
        OAuth2TokenIntrospectionResponse r = introspectOpaqueToken(token.trim(), hint);
        return ResponseEntity.ok(r);
    }

    /**
     * 【规范】RFC 7009 Token Revocation：客户端认证同令牌端点；成功时 200 空体（含令牌无效或已撤销）。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
     */
    @Override
    public ResponseEntity<?> processTokenRevocation(String authorizationHeader, OAuth2TokenRevocationRequest body) {
        OAuth2TokenRevocationRequest form = body != null ? body : new OAuth2TokenRevocationRequest();
        ParsedClientCredentials creds = parseClientCredentials(authorizationHeader, form.getClient_id(), form.getClient_secret());
        OAuth2RegisteredClientSnapshot caller = loadAndValidateClient(creds.clientId(), creds.clientSecret(), creds.authMethod());
        if (caller == null) {
            return invalidClient();
        }
        String token = form.getToken();
        if (!StringUtils.hasText(token)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "token is required");
        }
        String hint = form.getToken_type_hint();
        revokeOpaqueToken(token.trim(), hint);
        return ResponseEntity.ok().build();
    }

    /**
     * 在访问/刷新令牌持久化端口中撤销 access_token / refresh_token；{@code token_type_hint} 与 RFC 7009/7662 一致。
     */
    private void revokeOpaqueToken(String token, String tokenTypeHint) {
        boolean preferRefresh = OAuth2Rfc7662TokenTypeHints.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint);
        boolean preferAccess = OAuth2Rfc7662TokenTypeHints.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint);
        if (preferRefresh) {
            refreshTokenPersistence.removeRefreshToken(token);
            return;
        }
        if (preferAccess) {
            accessTokenPersistence.removeAccessToken(token);
            return;
        }
        if (refreshTokenPersistence.getRefreshToken(token).isPresent()) {
            refreshTokenPersistence.removeRefreshToken(token);
            return;
        }
        accessTokenPersistence.removeAccessToken(token);
    }

    /**
     * 在访问/刷新令牌持久化端口中解析 access_token / refresh_token；{@code token_type_hint} 与 RFC 7662 一致。
     */
    private OAuth2TokenIntrospectionResponse introspectOpaqueToken(String token, String tokenTypeHint) {
        boolean preferRefresh = OAuth2Rfc7662TokenTypeHints.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint);
        boolean preferAccess = OAuth2Rfc7662TokenTypeHints.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint);
        if (preferRefresh) {
            return refreshTokenPersistence.getRefreshToken(token)
                    .map(s -> toIntrospectionActive(s, OAuth2Rfc7662TokenTypeHints.REFRESH_TOKEN))
                    .orElseGet(OAuth2TokenIntrospectionResponse::inactive);
        }
        if (preferAccess) {
            return accessTokenPersistence.getAccessToken(token)
                    .map(s -> toIntrospectionActive(s, OAuth2Rfc7662TokenTypeHints.ACCESS_TOKEN))
                    .orElseGet(OAuth2TokenIntrospectionResponse::inactive);
        }
        Optional<OAuth2TokenSnapshot> at = accessTokenPersistence.getAccessToken(token);
        if (at.isPresent()) {
            return toIntrospectionActive(at.get(), OAuth2Rfc7662TokenTypeHints.ACCESS_TOKEN);
        }
        Optional<OAuth2TokenSnapshot> rt = refreshTokenPersistence.getRefreshToken(token);
        return rt.map(s -> toIntrospectionActive(s, OAuth2Rfc7662TokenTypeHints.REFRESH_TOKEN))
                .orElseGet(OAuth2TokenIntrospectionResponse::inactive);
    }

    private static OAuth2TokenIntrospectionResponse toIntrospectionActive(OAuth2TokenSnapshot snap, String tokenType) {
        String sub = StringUtils.hasText(snap.subject()) ? snap.subject() : null;
        return new OAuth2TokenIntrospectionResponse(
                true,
                StringUtils.hasText(snap.scope()) ? snap.scope() : null,
                snap.clientId(),
                sub,
                tokenType,
                sub,
                null,
                null);
    }

    /**
     * 【规范】RFC 6749 §4.4 Client Credentials Grant
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.4">RFC 6749 §4.4</a>
     */
    private ResponseEntity<?> grantClientCredentials(ParsedClientCredentials creds, OAuth2TokenRequest form) {
        OAuth2RegisteredClientSnapshot client = loadAndValidateClient(creds.clientId(), creds.clientSecret(), creds.authMethod());
        if (client == null) {
            return invalidClient();
        }
        if (!grantAllowed(client, OAuth2StandardGrantType.CLIENT_CREDENTIALS)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.UNAUTHORIZED_CLIENT, HttpStatus.BAD_REQUEST, "client_credentials not allowed");
        }
        String granted = resolveGrantedScope(client, form.getScope());
        if (granted == null) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_SCOPE, HttpStatus.BAD_REQUEST, "invalid scope");
        }
        return issueAccessTokenOnly(client, client.clientId(), granted);
    }

    /**
     * 【规范】RFC 6749 §4.3 Resource Owner Password Credentials Grant（若业务启用；OAuth 2.1 已弃用但本模块仍可按需暴露）
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.3">RFC 6749 §4.3</a>
     */
    private ResponseEntity<?> grantPassword(ParsedClientCredentials creds, OAuth2TokenRequest form) {
        OAuth2RegisteredClientSnapshot client = loadAndValidateClient(creds.clientId(), creds.clientSecret(), creds.authMethod());
        if (client == null) {
            return invalidClient();
        }
        if (!grantAllowed(client, OAuth2StandardGrantType.PASSWORD)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.UNAUTHORIZED_CLIENT, HttpStatus.BAD_REQUEST, "password grant not allowed");
        }
        String username = form.getUsername();
        String password = form.getPassword();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "username and password required");
        }
        String userId = resourceOwnerPassword.authenticate(username, password).orElse(null);
        if (userId == null) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "invalid username or password");
        }
        String granted = resolveGrantedScope(client, form.getScope());
        if (granted == null) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_SCOPE, HttpStatus.BAD_REQUEST, "invalid scope");
        }
        return issueAccessAndRefreshTokens(client, userId, granted);
    }

    /**
     * 【规范】RFC 6749 §6 Refreshing an Access Token
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-6">RFC 6749 §6</a>
     */
    private ResponseEntity<?> grantRefreshToken(ParsedClientCredentials creds, OAuth2TokenRequest form) {
        OAuth2RegisteredClientSnapshot client = loadAndValidateClient(creds.clientId(), creds.clientSecret(), creds.authMethod());
        if (client == null) {
            return invalidClient();
        }
        if (!grantAllowed(client, OAuth2StandardGrantType.REFRESH_TOKEN)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.UNAUTHORIZED_CLIENT, HttpStatus.BAD_REQUEST, "refresh_token grant not allowed");
        }
        String refreshToken = form.getRefresh_token();
        if (!StringUtils.hasText(refreshToken)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "refresh_token required");
        }
        var oldOpt = refreshTokenPersistence.getRefreshToken(refreshToken);
        if (oldOpt.isEmpty()) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "invalid refresh_token");
        }
        OAuth2TokenSnapshot old = oldOpt.get();
        if (!client.clientId().equals(old.clientId())) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "invalid refresh_token");
        }
        refreshTokenPersistence.removeRefreshToken(refreshToken);
        String scopeParam = form.getScope();
        String granted;
        if (!StringUtils.hasText(scopeParam)) {
            granted = old.scope() != null ? old.scope() : "";
        } else {
            granted = resolveGrantedScope(client, scopeParam);
            if (granted == null) {
                return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_SCOPE, HttpStatus.BAD_REQUEST, "invalid scope");
            }
        }
        return issueAccessAndRefreshTokens(client, old.subject(), granted);
    }

    /**
     * 【规范】RFC 6749 §4.1.3 Access Token Request（授权码换令牌）；PKCE 验证见 RFC 7636。
     * <p>
     * 【本模块定制】本方法<b>始终</b>要求表单含 {@code redirect_uri}（与 {@link #processAuthorizationRequest} 侧「始终要求授权请求带
     * {@code redirect_uri}」一致）。未按 §4.1.1/§4.1.3 字面实现「仅单回调时可省略」分支；理由见类级 JavaDoc「授权码流程与 {@code redirect_uri}」。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.1.3">RFC 6749 §4.1.3</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
     */
    private ResponseEntity<?> grantAuthorizationCode(ParsedClientCredentials creds, OAuth2TokenRequest form) {
        String code = form.getCode();
        String redirectUri = form.getRedirect_uri();
        // 与类级说明一致：本模块策略为始终要求 redirect_uri，非 RFC OPTIONAL 分支的条件实现
        if (!StringUtils.hasText(code) || !StringUtils.hasText(redirectUri)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "code and redirect_uri required");
        }
        String clientId = creds.clientId();
        if (!StringUtils.hasText(clientId)) {
            clientId = form.getClient_id();
        }
        OAuth2RegisteredClientSnapshot client = loadAndValidateClient(clientId, creds.clientSecret(), creds.authMethod());
        if (client == null) {
            return invalidClient();
        }
        if (!grantAllowed(client, OAuth2StandardGrantType.AUTHORIZATION_CODE)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.UNAUTHORIZED_CLIENT, HttpStatus.BAD_REQUEST, "authorization_code not allowed");
        }
        var recOpt = authorizationCodePersistence.consumeAuthorizationCode(code);
        if (recOpt.isEmpty()) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "invalid or expired code");
        }
        OAuth2AuthorizationCodeSnapshot rec = recOpt.get();
        if (!client.clientId().equals(rec.clientId())) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "invalid code");
        }
        if (!redirectUri.equals(rec.redirectUri())) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "redirect_uri mismatch");
        }
        String verifier = form.getCode_verifier();
        if (!OAuth2Pkce.verify(rec.codeChallenge(), rec.codeChallengeMethod(), verifier)) {
            return errorBody(OAuth2Rfc6749ErrorCodes.INVALID_GRANT, HttpStatus.BAD_REQUEST, "invalid code_verifier");
        }
        String granted = rec.scope() != null ? rec.scope() : "";
        return issueAccessAndRefreshTokens(client, rec.userId(), granted);
    }

    /**
     * 【规范】RFC 6749 §5.1（无 refresh_token 的成功响应子集）
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.1">RFC 6749 §5.1</a>
     */
    private ResponseEntity<?> issueAccessTokenOnly(OAuth2RegisteredClientSnapshot client, String subject, String grantedScope) {
        String accessToken = randomToken();
        long atSec = defaultLong(client.accessTokenTtlSeconds(), 7200L);
        accessTokenPersistence.putAccessToken(accessToken, new OAuth2TokenSnapshot(client.clientId(), subject, grantedScope), (int) atSec);
        return ResponseEntity.ok(OAuth2TokenSuccessResponse.accessOnly(accessToken, atSec, grantedScope));
    }

    /**
     * 【规范】RFC 6749 §5.1（含 refresh_token）
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.1">RFC 6749 §5.1</a>
     */
    private ResponseEntity<?> issueAccessAndRefreshTokens(OAuth2RegisteredClientSnapshot client, String subject, String grantedScope) {
        String accessToken = randomToken();
        String refreshToken = randomToken();
        long atSec = defaultLong(client.accessTokenTtlSeconds(), 7200L);
        long rtSec = defaultLong(client.refreshTokenTtlSeconds(), 2592000L);
        accessTokenPersistence.putAccessToken(accessToken, new OAuth2TokenSnapshot(client.clientId(), subject, grantedScope), (int) atSec);
        refreshTokenPersistence.putRefreshToken(refreshToken, new OAuth2TokenSnapshot(client.clientId(), subject, grantedScope), (int) rtSec);
        return ResponseEntity.ok(OAuth2TokenSuccessResponse.withRefresh(accessToken, atSec, refreshToken, grantedScope));
    }

    private static long defaultLong(Long v, long d) {
        return v != null && v > 0 ? v : d;
    }

    private OAuth2RegisteredClientSnapshot loadAndValidateClient(String clientId, String clientSecret, String authMethod) {
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        OAuth2RegisteredClientSnapshot c = clientRegistry.findEnabledByClientId(clientId).orElse(null);
        if (c == null) {
            return null;
        }
        if (!Objects.equals(c.clientSecret(), clientSecret)) {
            return null;
        }
        if (!clientAuthMethodAllowed(c, authMethod)) {
            return null;
        }
        return c;
    }

    private static boolean clientAuthMethodAllowed(OAuth2RegisteredClientSnapshot c, String used) {
        if (used == null) {
            return true;
        }
        Set<String> allow = nz(c.allowAuthenticationMethods());
        if (allow.isEmpty()) {
            return "client_secret_basic".equals(used) || "client_secret_post".equals(used);
        }
        for (String s : allow) {
            if (used.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private static boolean grantAllowed(OAuth2RegisteredClientSnapshot c, String grantType) {
        Set<String> g = nz(c.allowedGrantTypes());
        if (g.isEmpty()) {
            return false;
        }
        for (String s : g) {
            if (grantType.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private String resolveGrantedScope(OAuth2RegisteredClientSnapshot client, String scopeRequest) {
        Set<String> allowed = nz(client.allowedScopes());
        if (allowed.isEmpty()) {
            return !StringUtils.hasText(scopeRequest) ? "" : null;
        }
        if (!StringUtils.hasText(scopeRequest)) {
            return String.join(" ", allowed);
        }
        Set<String> requested = parseSpaceScopes(scopeRequest);
        for (String r : requested) {
            if (!allowed.contains(r)) {
                return null;
            }
        }
        return String.join(" ", requested);
    }

    private static Set<String> nz(Set<String> s) {
        return s == null ? Set.of() : s;
    }

    private static Set<String> parseSpaceScopes(String s) {
        return OAuth2ScopeStrings.parseSpaceSeparated(s);
    }

    /**
     * 【规范】RFC 6749 §2.3 Client Authentication：支持 {@code client_secret_basic} 与 {@code client_secret_post} 的常见形态。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-2.3">RFC 6749 §2.3</a>
     */
    private ParsedClientCredentials parseClientCredentials(String authorizationHeader, String clientId, String clientSecret) {
        if (authorizationHeader != null && authorizationHeader.regionMatches(true, 0, "Basic ", 0, 6)) {
            String b64 = authorizationHeader.substring(6).trim();
            byte[] decoded = Base64.getDecoder().decode(b64);
            String pair = new String(decoded, StandardCharsets.UTF_8);
            int colon = pair.indexOf(':');
            if (colon < 0) {
                return new ParsedClientCredentials(null, null, "client_secret_basic");
            }
            String cid = pair.substring(0, colon);
            String sec = pair.substring(colon + 1);
            return new ParsedClientCredentials(cid, sec, "client_secret_basic");
        }
        if (clientId != null && clientSecret != null) {
            return new ParsedClientCredentials(clientId, clientSecret, "client_secret_post");
        }
        return new ParsedClientCredentials(clientId, clientSecret, null);
    }

    /**
     * 【规范】RFC 6749 §5.2 Token Error Response（JSON 字段名与 HTTP 状态与常见实践一致）。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.2">RFC 6749 §5.2</a>
     */
    private static ResponseEntity<?> errorBody(String error, HttpStatus status, String desc) {
        return ResponseEntity.status(status).body(new OAuth2ErrorResponse(error, desc));
    }

    /**
     * 【规范】{@code invalid_client} + 401；{@code WWW-Authenticate} 为常见做法（RFC 6749 允许与实现相关）。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.2">RFC 6749 §5.2</a>
     */
    private static ResponseEntity<?> invalidClient() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"oauth2\"")
                .body(new OAuth2ErrorResponse(OAuth2Rfc6749ErrorCodes.INVALID_CLIENT, null));
    }

    /**
     * 【本模块定制】令牌值格式（长度、URL-safe Base64）；RFC 6749 未规定 access_token 具体编码。
     */
    private static String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // -------------------------------------------------------------------------
    // 【规范】RFC 6749 §4.1.1 / §4.1.2 Authorization Endpoint；PKCE：RFC 7636
    // 【本模块定制】未登录时的登录跳转、部分错误用 sendError —— 见类级 JavaDoc
    // -------------------------------------------------------------------------

    @Override
    public void processAuthorizationRequest(OAuth2AuthorizeRequest r, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 【本模块定制】空 DTO 防御；RFC 未规定此分支的媒体类型
        if (r == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid request");
            return;
        }
        String responseType = r.getResponse_type();
        String clientId = r.getClient_id();
        String redirectUri = r.getRedirect_uri();
        String scope = r.getScope();
        String state = r.getState();
        String codeChallenge = r.getCode_challenge();
        String codeChallengeMethod = r.getCode_challenge_method();

        if (!StringUtils.hasText(clientId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "client_id required");
            return;
        }
        OAuth2RegisteredClientSnapshot client = clientRegistry.findEnabledByClientId(clientId).orElse(null);
        if (client == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid client");
            return;
        }
        if (!grantAllowed(client, OAuth2StandardGrantType.AUTHORIZATION_CODE)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "authorization_code not allowed for client");
            return;
        }
        // 【本模块定制】RFC §4.1.1 允许「仅登记一个 redirect 时省略 query 中的 redirect_uri」；本模块始终要求显式传递，见类级 JavaDoc
        if (!StringUtils.hasText(redirectUri) || !redirectUriAllowed(client, redirectUri)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid redirect_uri");
            return;
        }
        if (!"code".equals(responseType)) {
            redirectOAuthError(response, redirectUri, state, OAuth2Rfc6749ErrorCodes.UNSUPPORTED_RESPONSE_TYPE, "only code supported");
            return;
        }
        String granted = resolveGrantedScope(client, scope);
        if (granted == null) {
            redirectOAuthError(response, redirectUri, state, OAuth2Rfc6749ErrorCodes.INVALID_SCOPE, "invalid scope");
            return;
        }
        // 【本模块定制】会话与登录页：由 SPI 决定；resume URL 供宿主登录后跳回 authorize
        if (endUserSession.currentUserId(request).isEmpty()) {
            String full = request.getRequestURL().toString();
            String qs = request.getQueryString();
            if (StringUtils.hasText(qs)) {
                full = full + "?" + qs;
            }
            loginNavigation.redirectToLogin(request, response, full);
            return;
        }
        String userId = endUserSession.currentUserId(request).orElse("");
        Set<String> scopeSet = parseSpaceScopes(granted);
        if (needsConsent(userId, client, scopeSet)) {
            OAuth2PendingAuthorization pend = new OAuth2PendingAuthorization(
                    client.clientId(),
                    userId,
                    redirectUri,
                    granted,
                    state,
                    codeChallenge,
                    codeChallengeMethod);
            request.getSession(true).setAttribute(OAuth2ServletSessionKeys.PENDING_AUTHORIZATION, pend);
            String cp = request.getContextPath() == null ? "" : request.getContextPath();
            String loc = cp + "/oauth2/consent";
            response.sendRedirect(response.encodeRedirectURL(loc));
            return;
        }
        issueAuthorizationCodeSuccess(client, userId, redirectUri, granted, state, codeChallenge, codeChallengeMethod, response);
    }

    private boolean needsConsent(String userId, OAuth2RegisteredClientSnapshot client, Set<String> requestedScopes) {
        if (!client.requireAuthorizationConsent()) {
            return false;
        }
        Set<String> exempt = nz(client.consentExemptScopes());
        if (!requestedScopes.isEmpty() && exempt.containsAll(requestedScopes)) {
            return false;
        }
        return !consentStorage.hasConsented(userId, client.clientId(), requestedScopes);
    }

    private void issueAuthorizationCodeSuccess(
            OAuth2RegisteredClientSnapshot client,
            String userId,
            String redirectUri,
            String granted,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            HttpServletResponse response)
            throws IOException {
        String code = randomToken();
        OAuth2AuthorizationCodeSnapshot rec = new OAuth2AuthorizationCodeSnapshot(
                client.clientId(),
                userId,
                redirectUri,
                granted,
                codeChallenge,
                codeChallengeMethod);
        authorizationCodePersistence.putAuthorizationCode(code, rec, properties.authorizationCodeTtlSeconds());
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(redirectUri);
        b.queryParam(OAuth2TokenRequest.Fields.code, code);
        if (state != null) {
            b.queryParam(OAuth2AuthorizeRequest.Fields.state, state);
        }
        response.sendRedirect(b.encode(StandardCharsets.UTF_8).toUriString());
    }

    private OAuth2PendingAuthorization peekPendingAuthorization(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object o = session.getAttribute(OAuth2ServletSessionKeys.PENDING_AUTHORIZATION);
        if (!(o instanceof OAuth2PendingAuthorization p)) {
            return null;
        }
        return p;
    }

    private void removePendingAuthorization(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(OAuth2ServletSessionKeys.PENDING_AUTHORIZATION);
        }
    }

    @Override
    public void processConsentGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OAuth2PendingAuthorization p = peekPendingAuthorization(request);
        if (p == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no pending authorization in session");
            return;
        }
        String current = endUserSession.currentUserId(request).orElse("");
        if (!p.userId().equals(current)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "session mismatch");
            return;
        }
        List<String> scopes = new ArrayList<>(parseSpaceScopes(p.grantedScope()));
        OAuth2ConsentViewModel vm = new OAuth2ConsentViewModel(
                p.clientId(),
                p.grantedScope() == null ? "" : p.grantedScope(),
                scopes,
                request.getContextPath() == null ? "" : request.getContextPath());
        consentUi.renderConsentPage(request, response, vm);
    }

    @Override
    public void processConsentPost(boolean approved, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        OAuth2PendingAuthorization p = peekPendingAuthorization(request);
        if (p == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no pending authorization in session");
            return;
        }
        String current = endUserSession.currentUserId(request).orElse("");
        if (!p.userId().equals(current)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "session mismatch");
            return;
        }
        removePendingAuthorization(request);
        OAuth2RegisteredClientSnapshot client = clientRegistry.findEnabledByClientId(p.clientId()).orElse(null);
        if (client == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid client");
            return;
        }
        if (!approved) {
            redirectOAuthError(response, p.redirectUri(), p.state(), OAuth2Rfc6749ErrorCodes.ACCESS_DENIED, "user denied");
            return;
        }
        Set<String> scopeSet = parseSpaceScopes(p.grantedScope());
        consentStorage.recordConsent(p.userId(), p.clientId(), scopeSet);
        issueAuthorizationCodeSuccess(
                client,
                p.userId(),
                p.redirectUri(),
                p.grantedScope(),
                p.state(),
                p.codeChallenge(),
                p.codeChallengeMethod(),
                response);
    }

    private static boolean redirectUriAllowed(OAuth2RegisteredClientSnapshot c, String redirectUri) {
        if (!StringUtils.hasText(redirectUri)) {
            return false;
        }
        Set<String> uris = nz(c.allowedRedirectUris());
        if (uris.isEmpty()) {
            return false;
        }
        for (String u : uris) {
            if (redirectUri.equals(u.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 【规范】RFC 6749 §4.1.2.1：通过重定向返回授权端点错误（query 参数）。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.1.2.1">RFC 6749 §4.1.2.1</a>
     */
    private void redirectOAuthError(HttpServletResponse response, String redirectUri, String state, String error, String desc)
            throws IOException {
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(redirectUri);
        b.queryParam(OAuth2ErrorResponse.Fields.error, error);
        if (desc != null) {
            b.queryParam(OAuth2ErrorResponse.Fields.error_description, desc);
        }
        if (state != null) {
            b.queryParam(OAuth2AuthorizeRequest.Fields.state, state);
        }
        response.sendRedirect(b.encode(StandardCharsets.UTF_8).toUriString());
    }

    // -------------------------------------------------------------------------
    // 【规范】RFC 8414 Authorization Server Metadata
    // 【本模块定制】端点路径相对于 issuer 的后缀固定为 /oauth2/authorize、/oauth2/token
    // -------------------------------------------------------------------------

    /**
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
     */
    @Override
    public OAuth2AuthorizationServerMetadataResponse buildAuthorizationServerMetadata(HttpServletRequest request) {
        String issuer = properties.resolveIssuer(request);
        return new OAuth2AuthorizationServerMetadataResponse(
                issuer,
                issuer + "/oauth2/authorize",
                issuer + "/oauth2/token",
                issuer + "/oauth2/introspect",
                issuer + "/oauth2/revoke",
                List.of("client_secret_basic", "client_secret_post"),
                List.of("code"),
                List.of(
                        OAuth2StandardGrantType.AUTHORIZATION_CODE,
                        OAuth2StandardGrantType.REFRESH_TOKEN,
                        OAuth2StandardGrantType.CLIENT_CREDENTIALS,
                        OAuth2StandardGrantType.PASSWORD),
                List.of("S256", "plain"),
                issuer + "/oauth2/consent");
    }
}
