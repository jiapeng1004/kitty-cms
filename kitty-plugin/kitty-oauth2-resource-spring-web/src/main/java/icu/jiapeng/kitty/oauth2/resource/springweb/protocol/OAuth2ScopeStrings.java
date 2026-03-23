package icu.jiapeng.kitty.oauth2.resource.springweb.protocol;

import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * OAuth2 {@code scope} 请求/响应中的空格分隔字符串解析（RFC 6749 常见形态）。
 */
public final class OAuth2ScopeStrings {

    private OAuth2ScopeStrings() {
    }

    /**
     * @param scope 空格分隔的 scope 串；空则返回空集
     */
    public static Set<String> parseSpaceSeparated(String scope) {
        if (!StringUtils.hasText(scope)) {
            return Set.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String p : scope.split("\\s+")) {
            String t = p.trim();
            if (StringUtils.hasText(t)) {
                set.add(t);
            }
        }
        return set;
    }
}
