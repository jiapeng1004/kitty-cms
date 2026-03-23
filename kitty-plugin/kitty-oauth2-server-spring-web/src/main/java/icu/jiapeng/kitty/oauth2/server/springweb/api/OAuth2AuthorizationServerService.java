package icu.jiapeng.kitty.oauth2.server.springweb.api;

import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2AuthorizeRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenIntrospectionRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.OAuth2TokenRevocationRequest;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2AuthorizationServerMetadataResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * OAuth2 授权服务器领域操作（由 {@link icu.jiapeng.kitty.oauth2.server.springweb.internal.OAuth2AuthorizationServerCoreService}
 * 实现，仅依赖 SPI，无具体存储技术）。
 * <p>
 * 各方法与 HTTP 端点的规范对应关系见 {@link OAuth2AuthorizationServerApi}；此处强调可编程调用时的同一套语义。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
public interface OAuth2AuthorizationServerService {

    /**
     * <b>【规范】</b>RFC 7009 令牌撤销：校验客户端凭据后，从持久化中删除 {@code token}；成功时 HTTP 200 空体。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
     */
    ResponseEntity<?> processTokenRevocation(String authorizationHeader, OAuth2TokenRevocationRequest body);

    /**
     * <b>【规范】</b>RFC 7662 令牌自省：校验调用方客户端凭据后，对 {@code token} 查询持久化中的 access / refresh 状态。
     * 客户端认证失败时返回体与 {@link #processTokenRequest(String, OAuth2TokenRequest)} 的 {@code invalid_client} 一致（401）。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
     */
    ResponseEntity<?> processTokenIntrospection(String authorizationHeader, OAuth2TokenIntrospectionRequest body);

    /**
     * <b>【规范】</b>同 {@link OAuth2AuthorizationServerApi#token(String, OAuth2TokenRequest)}（RFC 6749 §3.2 等）。
     * <p>
     * <b>【本模块定制】</b>无；参数与 HTTP 层一致。
     *
     * @param authorizationHeader 可选，RFC 6749 §2.3 常见客户端认证（如 {@code Basic}）
     * @return 成功体 {@link icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2TokenSuccessResponse}，
     *         失败体 {@link icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2ErrorResponse}
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
     */
    ResponseEntity<?> processTokenRequest(String authorizationHeader, OAuth2TokenRequest body);

    /**
     * <b>【规范】</b>同 {@link OAuth2AuthorizationServerApi#authorize(OAuth2AuthorizeRequest, HttpServletRequest, HttpServletResponse)}
     * （RFC 6749 §4.1.1 / §4.1.2 等）。
     * <p>
     * <b>【本模块定制】</b>{@link HttpServletRequest} 用于宿主登录跳转、issuer 无关的 URL 拼接，以及
     * {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort#currentUserId(HttpServletRequest)} 识别终端用户。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
     */
    void processAuthorizationRequest(OAuth2AuthorizeRequest authorize, HttpServletRequest request, HttpServletResponse response)
            throws IOException;

    /**
     * <b>【规范】</b>RFC 8414 授权服务器元数据 JSON。
     * <p>
     * <b>【本模块定制】</b>{@link HttpServletRequest} 用途同 {@link OAuth2AuthorizationServerApi#authorizationServerMetadata(HttpServletRequest)}。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
     */
    OAuth2AuthorizationServerMetadataResponse buildAuthorizationServerMetadata(HttpServletRequest request);

    /**
     * OAuth2 扩展：GET consent 页（展示待授权 scope）。
     * <p>
     * 待授权状态由 {@link jakarta.servlet.http.HttpSession} 持有（见 {@link icu.jiapeng.kitty.oauth2.server.springweb.internal.OAuth2ServletSessionKeys}）。
     */
    void processConsentGet(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * OAuth2 扩展：POST consent（同意或拒绝）。
     */
    void processConsentPost(boolean approved, HttpServletRequest request, HttpServletResponse response)
            throws IOException;
}
