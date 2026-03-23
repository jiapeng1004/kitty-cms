package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

/**
 * MyBatis-Plus 令牌表共享：过期时间与 token 哈希。
 */
final class OAuth2MybatisPlusPersistenceSupport {

    private OAuth2MybatisPlusPersistenceSupport() {}

    static LocalDateTime expiresAt(int ttlSeconds) {
        return LocalDateTime.now(ZoneOffset.UTC).plusSeconds(Math.max(0, ttlSeconds));
    }

    static boolean notExpired(LocalDateTime expiresAt) {
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now(ZoneOffset.UTC));
    }

    static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256", e);
        }
    }
}
