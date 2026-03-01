package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.AccessKeyVO;
import icu.jiapeng.kitty.transcoder.api.CreateAccessKeyRequest;
import icu.jiapeng.kitty.transcoder.api.CreateAccessKeyResponse;
import icu.jiapeng.kitty.transcoder.api.LoginRequest;
import icu.jiapeng.kitty.transcoder.api.LoginResponse;
import icu.jiapeng.kitty.transcoder.func.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 使用 AK/SK 登录，校验通过后返回 API Token（存入 Redis 会话）。
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        String accessKeyId = request.getAccessKeyId();
        String secretKey = request.getSecretKey();
        String storedSecret = authService.getSecretKey(accessKeyId);
        if (storedSecret == null || !storedSecret.equals(secretKey)) {
            throw new RuntimeException("Invalid Access Key or Secret Key");
        }
        String token = authService.generateLoginToken(accessKeyId);
        return new LoginResponse(token, accessKeyId);
    }

    @PostMapping("/access-key")
    public CreateAccessKeyResponse createAccessKey(@RequestBody CreateAccessKeyRequest request) {
        return authService.generateAccessKey(request != null ? request : new CreateAccessKeyRequest());
    }

    @GetMapping("/access-key")
    public List<AccessKeyVO> getAccessKeys() {
        return authService.listAccessKeys();
    }

    /**
     * 删除 Access Key（按 accessKeyId）
     */
    @DeleteMapping("/access-key/{accessKeyId}")
    public Boolean deleteAccessKey(@PathVariable("accessKeyId") String accessKeyId) {
        return authService.deleteAccessKey(accessKeyId);
    }
}
