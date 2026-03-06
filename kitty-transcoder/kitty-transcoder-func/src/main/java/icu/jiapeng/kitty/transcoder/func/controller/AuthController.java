package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "AK/SK 登录、Access Key 管理")
public class AuthController {

    @Resource
    private AuthService authService;

    @Operation(summary = "AK/SK 登录", description = "校验通过后返回 API Token，存入 Redis 会话")
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

    @Operation(summary = "创建 Access Key")
    @PostMapping("/access-key")
    public CreateAccessKeyResponse createAccessKey(@RequestBody CreateAccessKeyRequest request) {
        return authService.generateAccessKey(request != null ? request : new CreateAccessKeyRequest());
    }

    @Operation(summary = "查询 Access Key 列表")
    @GetMapping("/access-key")
    public List<AccessKeyVO> getAccessKeys() {
        return authService.listAccessKeys();
    }

    @Operation(summary = "删除 Access Key")
    @DeleteMapping("/access-key/{accessKeyId}")
    public Boolean deleteAccessKey(@PathVariable("accessKeyId") String accessKeyId) {
        return authService.deleteAccessKey(accessKeyId);
    }
}
