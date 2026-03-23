package icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus;

import icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus.entity.OAuth2AccessTokenRow;
import icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus.mapper.OAuth2AccessTokenResourceViewMapper;
import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

/**
 * 与授权服务器 MyBatis 持久化<strong>相同表与哈希规则</strong>的只读 access_token 校验。
 */
@RequiredArgsConstructor
public class MybatisPlusOAuth2AccessTokenValidationAdapter implements OAuth2AccessTokenValidationPort {

    private final OAuth2AccessTokenResourceViewMapper accessTokenMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<OAuth2TokenSnapshot> resolveAccessToken(String rawAccessToken) {
        if (!StringUtils.hasText(rawAccessToken)) {
            return Optional.empty();
        }
        String hash = sha256Hex(rawAccessToken.trim());
        OAuth2AccessTokenRow row = accessTokenMapper.selectById(hash);
        if (row == null || !notExpired(row.getExpiresAt())) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(row.getPayloadJson(), OAuth2TokenSnapshot.class));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }

    private static boolean notExpired(LocalDateTime expiresAt) {
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now(ZoneOffset.UTC));
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256", e);
        }
    }
}
