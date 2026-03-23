package icu.jiapeng.kitty.oauth2.server.springweb.extras.redis;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * 【可选默认】Redis：仅 access_token。
 */
@RequiredArgsConstructor
public class RedisOAuth2AccessTokenPersistenceAdapter implements OAuth2AccessTokenPersistencePort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void putAccessToken(String token, OAuth2TokenSnapshot payload, int ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(
                    OAuth2RedisTokenKeys.PREFIX_ACCESS_TOKEN + token,
                    json,
                    Duration.ofSeconds(ttlSeconds));
        } catch (JacksonException e) {
            throw new IllegalStateException("serialize access token payload", e);
        }
    }

    @Override
    public Optional<OAuth2TokenSnapshot> getAccessToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return Optional.empty();
        }
        String json = stringRedisTemplate.opsForValue().get(OAuth2RedisTokenKeys.PREFIX_ACCESS_TOKEN + accessToken.trim());
        if (!StringUtils.hasText(json)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, OAuth2TokenSnapshot.class));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }

    @Override
    public void removeAccessToken(String accessToken) {
        if (StringUtils.hasText(accessToken)) {
            stringRedisTemplate.delete(OAuth2RedisTokenKeys.PREFIX_ACCESS_TOKEN + accessToken.trim());
        }
    }
}
