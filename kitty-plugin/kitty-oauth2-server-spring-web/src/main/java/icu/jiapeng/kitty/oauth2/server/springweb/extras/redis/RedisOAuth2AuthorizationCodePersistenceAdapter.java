package icu.jiapeng.kitty.oauth2.server.springweb.extras.redis;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2AuthorizationCodeSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * 【可选默认】Redis：仅授权码。
 */
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationCodePersistenceAdapter implements OAuth2AuthorizationCodePersistencePort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void putAuthorizationCode(String code, OAuth2AuthorizationCodeSnapshot payload, int ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(
                    OAuth2RedisTokenKeys.PREFIX_AUTH_CODE + code,
                    json,
                    Duration.ofSeconds(ttlSeconds));
        } catch (JacksonException e) {
            throw new IllegalStateException("serialize authorization code payload", e);
        }
    }

    @Override
    public Optional<OAuth2AuthorizationCodeSnapshot> consumeAuthorizationCode(String code) {
        String key = OAuth2RedisTokenKeys.PREFIX_AUTH_CODE + code;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(json)) {
            return Optional.empty();
        }
        stringRedisTemplate.delete(key);
        try {
            return Optional.of(objectMapper.readValue(json, OAuth2AuthorizationCodeSnapshot.class));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }
}
