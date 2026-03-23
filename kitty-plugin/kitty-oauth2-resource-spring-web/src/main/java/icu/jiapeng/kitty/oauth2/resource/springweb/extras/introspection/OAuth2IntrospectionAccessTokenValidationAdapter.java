package icu.jiapeng.kitty.oauth2.resource.springweb.extras.introspection;

import icu.jiapeng.kitty.oauth2.resource.springweb.dto.OAuth2TokenIntrospectionResponse;
import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import icu.jiapeng.kitty.oauth2.resource.springweb.properties.OAuth2ResourceIntrospectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 通过 RFC 7662 自省端点解析 Bearer access_token（{@link RestTemplate}）。
 * <p>
 * 默认由自动配置注入；宿主可声明同名 Bean
 */
@RequiredArgsConstructor
public class OAuth2IntrospectionAccessTokenValidationAdapter implements OAuth2AccessTokenValidationPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OAuth2ResourceIntrospectionProperties properties;

    @Override
    public Optional<OAuth2TokenSnapshot> resolveAccessToken(String rawAccessToken) {
        if (!StringUtils.hasText(rawAccessToken)) {
            return Optional.empty();
        }
        OAuth2TokenIntrospectionResponse r = postIntrospect(rawAccessToken.trim(), "access_token");
        return toSnapshot(r);
    }

    private OAuth2TokenIntrospectionResponse postIntrospect(String token, @SuppressWarnings("all") String tokenTypeHint) {
        String introspectionEndpoint = properties.getEndpoint();
        String clientId = properties.getClientId();
        String clientSecret = properties.getClientSecret();
        if (!StringUtils.hasText(introspectionEndpoint) || !StringUtils.hasText(clientId)) {
            return OAuth2TokenIntrospectionResponse.inactive();
        }
        try {
            String body =
                    "token="
                            + URLEncoder.encode(token, StandardCharsets.UTF_8)
                            + (StringUtils.hasText(tokenTypeHint)
                            ? "&token_type_hint=" + URLEncoder.encode(tokenTypeHint.trim(), StandardCharsets.UTF_8)
                            : "");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId.trim(), clientSecret != null ? clientSecret : "");
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response =
                    restTemplate.exchange(
                            URI.create(introspectionEndpoint.trim()),
                            HttpMethod.POST,
                            entity,
                            byte[].class);
            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null
                    || response.getBody().length == 0) {
                return OAuth2TokenIntrospectionResponse.inactive();
            }
            return objectMapper.readValue(response.getBody(), OAuth2TokenIntrospectionResponse.class);
        } catch (Exception e) {
            return OAuth2TokenIntrospectionResponse.inactive();
        }
    }

    private static Optional<OAuth2TokenSnapshot> toSnapshot(OAuth2TokenIntrospectionResponse r) {
        if (r == null || !Boolean.TRUE.equals(r.active())) {
            return Optional.empty();
        }
        String sub = StringUtils.hasText(r.sub()) ? r.sub() : r.username();
        String scope = StringUtils.hasText(r.scope()) ? r.scope() : "";
        return Optional.of(new OAuth2TokenSnapshot(r.clientId(), sub, scope));
    }
}
