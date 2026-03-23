package icu.jiapeng.kitty.oauth2.server.springweb.model;

/**
 * 授权端点因需用户同意而暂存的状态，写入 {@link jakarta.servlet.http.HttpSession} 后在 consent 端点读取。
 */
public record OAuth2PendingAuthorization(
        String clientId,
        String userId,
        String redirectUri,
        String grantedScope,
        String state,
        String codeChallenge,
        String codeChallengeMethod
) {
}
