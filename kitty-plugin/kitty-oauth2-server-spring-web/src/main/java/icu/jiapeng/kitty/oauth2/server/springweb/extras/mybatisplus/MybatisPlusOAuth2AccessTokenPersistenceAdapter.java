package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus;

import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity.OAuth2AccessTokenRow;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2AccessTokenMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.expiresAt;
import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.notExpired;
import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.sha256Hex;

/**
 * 【可选默认】MyBatis-Plus：仅 access_token 表。
 */
@RequiredArgsConstructor
public class MybatisPlusOAuth2AccessTokenPersistenceAdapter implements OAuth2AccessTokenPersistencePort {

    private final OAuth2AccessTokenMapper accessTokenMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void putAccessToken(String token, OAuth2TokenSnapshot payload, int ttlSeconds) {
        try {
            OAuth2AccessTokenRow row = new OAuth2AccessTokenRow();
            row.setTokenHash(sha256Hex(token));
            row.setPayloadJson(objectMapper.writeValueAsString(payload));
            row.setExpiresAt(expiresAt(ttlSeconds));
            upsertAccessToken(row);
        } catch (JacksonException e) {
            throw new IllegalStateException("serialize access token payload", e);
        }
    }

    @Override
    public Optional<OAuth2TokenSnapshot> getAccessToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return Optional.empty();
        }
        String hash = sha256Hex(accessToken.trim());
        OAuth2AccessTokenRow row = accessTokenMapper.selectById(hash);
        if (row == null || !notExpired(row.getExpiresAt())) {
            if (row != null) {
                accessTokenMapper.deleteById(hash);
            }
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(row.getPayloadJson(), OAuth2TokenSnapshot.class));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }

    @Override
    public void removeAccessToken(String accessToken) {
        if (StringUtils.hasText(accessToken)) {
            accessTokenMapper.deleteById(sha256Hex(accessToken.trim()));
        }
    }

    private void upsertAccessToken(OAuth2AccessTokenRow row) {
        if (accessTokenMapper.selectById(row.getTokenHash()) == null) {
            accessTokenMapper.insert(row);
        } else {
            accessTokenMapper.updateById(row);
        }
    }
}
