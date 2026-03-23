package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity.OAuth2UserClientConsentEntity;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2UserClientConsentMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort;
import icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2ScopeStrings;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 【可选默认】使用 MyBatis-Plus 持久化用户-客户端已同意 scope（表 {@code kt_oauth2_user_client_consent}）。
 */
@RequiredArgsConstructor
public class MybatisPlusOAuth2ConsentStorageAdapter implements OAuth2ConsentStoragePort {

    private final OAuth2UserClientConsentMapper mapper;

    @Override
    public boolean hasConsented(String userId, String clientId, Set<String> requestedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            return true;
        }
        OAuth2UserClientConsentEntity row = selectOne(userId, clientId);
        if (row == null || !StringUtils.hasText(row.getScopes())) {
            return false;
        }
        Set<String> have = OAuth2ScopeStrings.parseSpaceSeparated(row.getScopes());
        return have.containsAll(requestedScopes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordConsent(String userId, String clientId, Set<String> grantedScopes) {
        if (grantedScopes == null || grantedScopes.isEmpty()) {
            return;
        }
        OAuth2UserClientConsentEntity row = selectOne(userId, clientId);
        Set<String> merged = new HashSet<>(OAuth2ScopeStrings.parseSpaceSeparated(row == null ? "" : row.getScopes()));
        merged.addAll(grantedScopes);
        String scopes = String.join(" ", merged);
        if (row == null) {
            OAuth2UserClientConsentEntity e = new OAuth2UserClientConsentEntity();
            e.setUserId(userId);
            e.setClientId(clientId);
            e.setScopes(scopes);
            mapper.insert(e);
        } else {
            row.setScopes(scopes);
            mapper.updateById(row);
        }
    }

    private OAuth2UserClientConsentEntity selectOne(String userId, String clientId) {
        LambdaQueryWrapper<OAuth2UserClientConsentEntity> w = new LambdaQueryWrapper<>();
        w.eq(OAuth2UserClientConsentEntity::getUserId, userId).eq(OAuth2UserClientConsentEntity::getClientId, clientId);
        return mapper.selectOne(w);
    }
}
