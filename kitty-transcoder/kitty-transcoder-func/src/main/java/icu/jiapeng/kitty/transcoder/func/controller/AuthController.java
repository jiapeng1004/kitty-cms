package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.func.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam("accessKey") String accessKey, 
                                   @RequestParam("signature") String signature, 
                                   @RequestParam("timestamp") long timestamp) {
        // 验证 AK/SK
        boolean valid = authService.validateAccessKey(accessKey);
        if (!valid) {
            throw new RuntimeException("Invalid Access Key");
        }

        // 生成登录 Token
        String token = authService.generateLoginToken(accessKey);

        // 返回 Token
        return Map.of("token", token, "accessKey", accessKey);
    }

    @PostMapping("/access-key")
    public Map<String, String> createAccessKey(@RequestBody Map<String, String> request) {
        String description = request.getOrDefault("description", "");
        return authService.generateAccessKey(description);
    }

    @GetMapping("/access-key")
    public Map<String, String> getAccessKeys() {
        // 这里可以实现获取 Access Key 列表的逻辑
        // 暂时返回空 Map
        return Map.of();
    }
}
