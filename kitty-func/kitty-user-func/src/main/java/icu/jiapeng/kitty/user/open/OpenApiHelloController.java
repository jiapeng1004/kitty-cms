package icu.jiapeng.kitty.user.open;


import icu.jiapeng.kitty.oauth2.resource.springweb.security.CheckScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/22
 */
@RequestMapping("/open/hello")
@RestController
public class OpenApiHelloController {
    @CheckScope("hello")
    @GetMapping
    public String hello() {
        return "hello kitty-cms";
    }
}
