package icu.jiapeng.kitty.oauth2.server.springweb.extras.satoken;

import cn.dev33.satoken.stp.StpUtil;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 【可选默认】授权/consent 端点终端用户：从请求 Cookie、自定义请求头或 {@code Authorization: Bearer} 解析 Sa-Token，
 * 经 {@link StpUtil#getLoginIdByToken(String)} 得到主体；若无显式 token 则回退 {@link StpUtil#isLogin()}（与 Sa 过滤器写入的线程上下文一致）。
 * <p>
 * 仅当 {@code extras.end-user-session} 为 {@code satoken} 或 {@code auto}、classpath 存在 Sa-Token，
 * 且未自定义 {@link OAuth2EndUserSessionPort} Bean 时由自动配置注册。
 *
 */
@RequiredArgsConstructor
public class SaTokenOAuth2EndUserSessionAdapter implements OAuth2EndUserSessionPort {

    @Override
    public Optional<String> currentUserId(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            Object id = StpUtil.getLoginId();
            return Optional.ofNullable(id).map(Object::toString).filter(StringUtils::hasText);
        }
        return Optional.empty();
    }
}
