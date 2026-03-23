package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus;

import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity.OAuth2AuthorizationCodeRow;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2AuthorizationCodeMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2AuthorizationCodeSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.expiresAt;
import static icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.OAuth2MybatisPlusPersistenceSupport.notExpired;

/**
 * 【可选默认】MyBatis-Plus：仅授权码表。
 */
@RequiredArgsConstructor
public class MybatisPlusOAuth2AuthorizationCodePersistenceAdapter implements OAuth2AuthorizationCodePersistencePort {

    private final OAuth2AuthorizationCodeMapper authorizationCodeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void putAuthorizationCode(String code, OAuth2AuthorizationCodeSnapshot payload, int ttlSeconds) {
        try {
            OAuth2AuthorizationCodeRow row = new OAuth2AuthorizationCodeRow();
            row.setCode(code);
            row.setPayloadJson(objectMapper.writeValueAsString(payload));
            row.setExpiresAt(expiresAt(ttlSeconds));
            upsertAuthorizationCode(row);
        } catch (JacksonException e) {
            throw new IllegalStateException("serialize authorization code payload", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<OAuth2AuthorizationCodeSnapshot> consumeAuthorizationCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        OAuth2AuthorizationCodeRow row = authorizationCodeMapper.selectById(code.trim());
        if (row == null) {
            return Optional.empty();
        }
        if (!notExpired(row.getExpiresAt())) {
            authorizationCodeMapper.deleteById(code.trim());
            return Optional.empty();
        }
        authorizationCodeMapper.deleteById(code.trim());
        try {
            return Optional.of(objectMapper.readValue(row.getPayloadJson(), OAuth2AuthorizationCodeSnapshot.class));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }

    private void upsertAuthorizationCode(OAuth2AuthorizationCodeRow row) {
        if (authorizationCodeMapper.selectById(row.getCode()) == null) {
            authorizationCodeMapper.insert(row);
        } else {
            authorizationCodeMapper.updateById(row);
        }
    }
}
