package icu.jiapeng.kitty.user.api.open.api;

import icu.jiapeng.kitty.user.api.open.OpenAuthCallbackParams;
import icu.jiapeng.kitty.user.api.open.OpenAuthRenderParams;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

public interface OpenAuthApi {
    @GetMapping("/open/auth/check/{source}")
    Boolean check(@PathVariable String source);

    @GetMapping("/open/auth/render/{source}")
    void render(
            @PathVariable String source,
            OpenAuthRenderParams params,
            HttpServletResponse response
    ) throws IOException;

    @GetMapping("/open/auth/callback/{source}")
    void callback(
            @PathVariable String source,
            OpenAuthCallbackParams params,
            HttpServletResponse response
    ) throws IOException;

    @GetMapping("/open/auth/callback/{source}/{next}")
    void callbackNext(
            @PathVariable String source,
            @PathVariable String next,
            OpenAuthCallbackParams params,
            HttpServletResponse response
    ) throws IOException;
}
