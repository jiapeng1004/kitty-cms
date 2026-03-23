package icu.jiapeng.kitty.oauth2.server.springweb.protocol;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * <b>【规范】</b>RFC 7636 Proof Key for Code Exchange：{@code code_verifier} 与 {@code code_challenge} 校验（{@code S256} / {@code plain}）。
 * <p>
 * <b>【本模块定制】</b>无；纯算法辅助类。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
public final class OAuth2Pkce {

    private OAuth2Pkce() {
    }

    public static boolean verify(String codeChallenge, String codeChallengeMethod, String codeVerifier) {
        if (codeChallenge == null || codeChallenge.isEmpty()) {
            return codeVerifier == null || codeVerifier.isEmpty();
        }
        if (codeVerifier == null || codeVerifier.isEmpty()) {
            return false;
        }
        String method = (codeChallengeMethod == null || codeChallengeMethod.isEmpty()) ? "plain" : codeChallengeMethod;
        if ("plain".equalsIgnoreCase(method)) {
            return codeChallenge.equals(codeVerifier);
        }
        if ("S256".equalsIgnoreCase(method)) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
                return codeChallenge.equals(encoded);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }
        return false;
    }
}
