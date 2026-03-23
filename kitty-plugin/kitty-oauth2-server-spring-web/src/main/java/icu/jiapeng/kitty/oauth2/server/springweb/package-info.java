/**
 * Kitty OAuth2 授权服务器插件（仅依赖 Spring Web，持久化/会话等由 SPI 注入）。
 * <h2>规范与定制：注释约定</h2>
 * <p>
 * 本包内代码与注释区分两类内容，便于与 RFC 对照阅读：
 * </p>
 * <ul>
 *   <li><b>【规范】</b>：与公开规范对齐的端点路径语义、HTTP 方法、表单/查询参数名、JSON 字段名、错误码与重定向错误参数等
 *      （主要依据 RFC 6749、6750、7636、8414）。</li>
 *   <li><b>【本模块定制】</b>：规范未规定或允许实现自由选择的细节在本模块中的固定做法（例如具体 URL 路径片段、DTO 形态、
 *       SPI 接口名、部分错误用 {@code sendError} 而非 OAuth JSON 等）。定制不改变与规范兼容的协议面，但在文档与协作上单独标明。</li>
 * </ul>
 * <p>
 * 各 HTTP 端点以 {@link icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerApi} 为契约入口；
 * 核心逻辑见 {@link icu.jiapeng.kitty.oauth2.server.springweb.internal.OAuth2AuthorizationServerCoreService}。
 * <p>
 * 官方规范文本（IETF RFC Editor）：
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749 — The OAuth 2.0 Authorization Framework</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750 — Bearer Token Usage</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636 — PKCE</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414 — Authorization Server Metadata</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
package icu.jiapeng.kitty.oauth2.server.springweb;
