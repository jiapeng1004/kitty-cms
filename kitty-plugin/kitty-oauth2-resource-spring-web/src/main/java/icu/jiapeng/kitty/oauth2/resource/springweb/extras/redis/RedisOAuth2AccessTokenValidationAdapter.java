package icu.jiapeng.kitty.oauth2.resource.springweb.extras.redis;

import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * 与授权服务器 Redis 持久化<strong>相同键</strong>的只读 access_token 校验。
 */
@RequiredArgsConstructor
public class RedisOAuth2AccessTokenValidationAdapter implements OAuth2AccessTokenValidationPort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<OAuth2TokenSnapshot> resolveAccessToken(String rawAccessToken) {
        if (!StringUtils.hasText(rawAccessToken)) {
            return Optional.empty();
        }
        String json =
                stringRedisTemplate.opsForValue().get(OAuth2RedisTokenKeys.PREFIX_ACCESS_TOKEN + rawAccessToken.trim());
        if (!StringUtils.hasText(json)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, OAuth2TokenSnapshot.class));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }
}
