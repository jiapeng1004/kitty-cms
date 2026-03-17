package icu.jiapeng.kitty.dns;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 仅当 `kitty.local-dns` 下存在至少一个映射 alias -> host:port 时才启用相关 Bean。
 */
public class LocalDnsEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        LocalDnsProperties properties = Binder.get(environment)
                .bind("kitty", Bindable.of(LocalDnsProperties.class))
                .orElse(null);
        return properties != null && properties.isEnabled();
    }
}

