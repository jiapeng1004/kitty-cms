package icu.jiapeng.kitty.oauth2.server.springweb.protocol;

/**
 * OAuth 相关 IETF 规范在 <a href="https://www.rfc-editor.org/">RFC Editor</a> 上的<strong>官方</strong>文本链接常量，
 * 供业务代码与 Javadoc {@code @see} 统一引用（避免各处手写 URL 不一致）。
 * <p>
 * Javadoc 中推荐写法示例：
 * <pre>{@code
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * }</pre>
 * 或直接 {@code @see} 本类字段（生成文档时可导航至常量定义）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749 — The OAuth 2.0 Authorization Framework</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750 — The OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636 — Proof Key for Code Exchange by OAuth Public Clients</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414 — OAuth 2.0 Authorization Server Metadata</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662 — OAuth 2.0 Token Introspection</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009 — OAuth 2.0 Token Revocation</a>
 */
public final class OAuth2OfficialSpecifications {

    /** RFC 6749 官方 URL。 */
    public static final String RFC_6749 = "https://www.rfc-editor.org/rfc/rfc6749";

    /** RFC 6750 官方 URL。 */
    public static final String RFC_6750 = "https://www.rfc-editor.org/rfc/rfc6750";

    /** RFC 7636 官方 URL。 */
    public static final String RFC_7636 = "https://www.rfc-editor.org/rfc/rfc7636";

    /** RFC 8414 官方 URL。 */
    public static final String RFC_8414 = "https://www.rfc-editor.org/rfc/rfc8414";

    /** RFC 7662 官方 URL。 */
    public static final String RFC_7662 = "https://www.rfc-editor.org/rfc/rfc7662";

    /** RFC 7009 官方 URL。 */
    public static final String RFC_7009 = "https://www.rfc-editor.org/rfc/rfc7009";

    private OAuth2OfficialSpecifications() {
    }
}
