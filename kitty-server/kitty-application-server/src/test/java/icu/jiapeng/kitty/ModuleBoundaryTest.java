package icu.jiapeng.kitty;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModuleBoundaryTest {

    ApplicationModules modules = ApplicationModules.of(KittyServerApplication.class);

    @Test
    void verifyModuleBoundaries() {
        modules.verify();  // 一行代码，自动检查所有模块边界
    }
}