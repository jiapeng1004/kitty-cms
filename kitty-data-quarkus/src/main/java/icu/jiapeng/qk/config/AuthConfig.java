package icu.jiapeng.qk.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "auth")
//@ConfigRoot(
//        phase = ConfigPhase.RUN_TIME
//)
@StaticInitSafe
public interface AuthConfig {

    @WithDefault("60")
    int refresh();

    @WithDefault("300")
    int interval();
}
