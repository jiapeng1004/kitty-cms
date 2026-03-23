package icu.jiapeng.kitty.user.oauth2.adapter.server;

import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ResourceOwnerPasswordPort;
import icu.jiapeng.kitty.user.user.service.KtUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 密码模式：委托 {@link KtUserService}（无验证码）。
 */
@Component
public class KtUserOAuth2ResourceOwnerPasswordAdapter implements OAuth2ResourceOwnerPasswordPort {

    @Resource
    private KtUserService ktUserService;

    @Override
    public Optional<String> authenticate(String username, String password) {
        return Optional.ofNullable(ktUserService.authenticateForOAuth2PasswordGrant(username, password));
    }
}
