package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus;

import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity.OAuth2RefreshTokenRow;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2RefreshTokenMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.expiresAt;
import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.notExpired;
import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.sha256Hex;

/**
 * 【可选默认】MyBatis-Plus：仅 refresh_token 表。
 */
@RequiredArgsConstructor
public class MybatisPlusOAuth2RefreshTokenPersistenceAdapter implements OAuth2RefreshTokenPersistencePort {

    private final OAuth2RefreshTokenMapper refreshTokenMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void putRefreshToken(String token, OAuth2TokenSnapshot payload, int ttlSeconds) {
        try {
            OAuth2RefreshTokenRow row = new OAuth2RefreshTokenRow();
            row.setTokenHash(sha256Hex(token));
            row.setPayloadJson(objectMapper.writeValueAsString(payload));
            row.setExpiresAt(expiresAt(ttlSeconds));
            upsertRefreshToken(row);
        } catch (JacksonException e) {
            throw new IllegalStateException("serialize refresh token payload", e);
        }
    }

    @Override
    public Optional<OAuth2TokenSnapshot> getRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return Optional.empty();
        }
        String hash = sha256Hex(refreshToken.trim());
        OAuth2RefreshTokenRow row = refreshTokenMapper.selectById(hash);
        if (row == null || !notExpired(row.getExpiresAt())) {
            if (row != null) {
                refreshTokenMapper.deleteById(hash);
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
    public void removeRefreshToken(String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenMapper.deleteById(sha256Hex(refreshToken.trim()));
        }
    }

    private void upsertRefreshToken(OAuth2RefreshTokenRow row) {
        if (refreshTokenMapper.selectById(row.getTokenHash()) == null) {
            refreshTokenMapper.insert(row);
        } else {
            refreshTokenMapper.updateById(row);
        }
    }
}
