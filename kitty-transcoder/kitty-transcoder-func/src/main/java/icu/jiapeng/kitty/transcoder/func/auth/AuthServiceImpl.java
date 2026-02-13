package icu.jiapeng.kitty.transcoder.func.auth;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String ACCESS_KEY_MAP_KEY = "transcode:auth:access_key";
    private static final String TOKEN_MAP_KEY = "transcode:auth:token";
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Map<String, String> generateAccessKey(String description) {
        // 生成 Access Key
        String accessKey = "AK" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        // 生成 Secret Key
        String secretKey = "SK" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        // 存储 AK/SK 对
        RMap<String, String> accessKeyMap = redissonClient.getMap(ACCESS_KEY_MAP_KEY);
        accessKeyMap.put(accessKey, secretKey);

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("accessKey", accessKey);
        result.put("secretKey", secretKey);
        result.put("description", description);

        return result;
    }

    @Override
    public boolean validateAccessKey(String accessKey) {
        RMap<String, String> accessKeyMap = redissonClient.getMap(ACCESS_KEY_MAP_KEY);
        return accessKeyMap.containsKey(accessKey);
    }

    @Override
    public String getSecretKey(String accessKey) {
        RMap<String, String> accessKeyMap = redissonClient.getMap(ACCESS_KEY_MAP_KEY);
        return accessKeyMap.get(accessKey);
    }

    @Override
    public String generateSignature(String accessKey, String secretKey, Map<String, String> params, long timestamp) {
        try {
            // 构建签名字符串
            StringBuilder sb = new StringBuilder();
            sb.append(accessKey).append("|");
            sb.append(timestamp).append("|");

            // 按 ASCII 正序拼接参数
            params.keySet().stream()
                    .sorted()
                    .forEach(key -> sb.append(key).append("=").append(params.get(key)).append("&"));

            // 移除最后一个 &
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
                sb.setLength(sb.length() - 1);
            }

            // 生成 HMAC-SHA1 签名
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(sb.toString().getBytes());

            // Base64 编码
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    @Override
    public boolean validateSignature(String accessKey, String signature, Map<String, String> params, long timestamp) {
        // 检查时间戳是否过期（5分钟）
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > 5 * 60 * 1000) {
            return false;
        }

        // 获取 Secret Key
        String secretKey = getSecretKey(accessKey);
        if (secretKey == null) {
            return false;
        }

        // 生成签名并验证
        String expectedSignature = generateSignature(accessKey, secretKey, params, timestamp);
        return expectedSignature.equals(signature);
    }

    @Override
    public String generateLoginToken(String accessKey) {
        // 生成 Token
        String token = "TOKEN" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        // 存储 Token
        RMap<String, String> tokenMap = redissonClient.getMap(TOKEN_MAP_KEY);
        tokenMap.put(token, accessKey);

        return token;
    }

    @Override
    public boolean validateLoginToken(String token) {
        RMap<String, String> tokenMap = redissonClient.getMap(TOKEN_MAP_KEY);
        return tokenMap.containsKey(token);
    }
}