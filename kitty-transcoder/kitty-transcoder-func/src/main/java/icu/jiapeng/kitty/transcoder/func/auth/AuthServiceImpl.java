package icu.jiapeng.kitty.transcoder.func.auth;

import icu.jiapeng.kitty.transcoder.api.AccessKeyVO;
import icu.jiapeng.kitty.transcoder.api.CreateAccessKeyRequest;
import icu.jiapeng.kitty.transcoder.api.CreateAccessKeyResponse;
import icu.jiapeng.kitty.transcoder.func.config.AuthConfig;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeAccessKey;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeAccessKeyMapper;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String SESSION_KEY_PREFIX = "transcode:session:token:";
    private static final String NONCE_KEY_PREFIX = "transcode:signature:nonce:";

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TranscodeAccessKeyMapper accessKeyMapper;
    @Autowired
    private AuthConfig authConfig;

    @Override
    public CreateAccessKeyResponse generateAccessKey(CreateAccessKeyRequest request) {
        String name = request != null && request.getName() != null ? request.getName().trim() : "";
        String accessKeyId = "AK" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String secretKey = "SK" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        TranscodeAccessKey entity = new TranscodeAccessKey();
        entity.setAccessKeyId(accessKeyId);
        entity.setSecretKey(secretKey);
        entity.setName(name);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        accessKeyMapper.insert(entity);
        return new CreateAccessKeyResponse(accessKeyId, secretKey, name);
    }

    @Override
    public boolean validateAccessKey(String accessKey) {
        return accessKeyMapper.selectOne(
                new LambdaQueryWrapper<TranscodeAccessKey>()
                        .eq(TranscodeAccessKey::getAccessKeyId, accessKey)
                        .eq(TranscodeAccessKey::getStatus, "ACTIVE")) != null;
    }

    @Override
    public String getSecretKey(String accessKey) {
        TranscodeAccessKey entity = accessKeyMapper.selectOne(
                new LambdaQueryWrapper<TranscodeAccessKey>()
                        .eq(TranscodeAccessKey::getAccessKeyId, accessKey));
        return entity == null ? null : entity.getSecretKey();
    }

    @Override
    public String generateSignature(String accessKey, String secretKey, Map<String, String> params, long timestamp) {
        params = new HashMap<>(params);
        params.put("AccessKeyId", accessKey);
        params.put("Timestamp", String.valueOf(timestamp));
        params.remove("Signature");
        String canonical = SignatureVerifier.buildCanonicalizedQueryString(params);
        String stringToSign = SignatureVerifier.buildStringToSign("GET", canonical);
        try {
            return SignatureVerifier.computeSignature(stringToSign, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    @Override
    public boolean validateSignature(String accessKey, String signature, Map<String, String> params, long timestamp) {
        int drift = authConfig.getSignature().getTimestampDriftSeconds();
        long nowSec = System.currentTimeMillis() / 1000;
        if (Math.abs(nowSec - timestamp) > drift) return false;
        String secretKey = getSecretKey(accessKey);
        if (secretKey == null) return false;
        String expected = generateSignature(accessKey, secretKey, params, timestamp);
        return expected != null && expected.equals(signature);
    }

    @Override
    public String generateLoginToken(String accessKey) {
        String token = "TOKEN" + UUID.randomUUID().toString().replace("-", "");
        RMap<String, Object> session = redissonClient.getMap(SESSION_KEY_PREFIX + token);
        session.put("accessKeyId", accessKey);
        session.put("createdAt", System.currentTimeMillis());
        long ttl = authConfig.getSession().getTtlSeconds();
        session.expire(ttl, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public boolean validateLoginToken(String token) {
        RMap<String, Object> session = redissonClient.getMap(SESSION_KEY_PREFIX + token);
        return session.isExists();
    }

    /**
     * 校验签名并消费 Nonce（防重放）。若通过则将 nonce 写入 Redis 并设置 TTL。
     */
    public boolean validateSignatureAndConsumeNonce(String accessKeyId, String signature, String signatureNonce, long timestampSec, Map<String, String> allParams) {
        String nonceKey = NONCE_KEY_PREFIX + signatureNonce;
        RBucket<String> bucket = redissonClient.getBucket(nonceKey);
        if (bucket.isExists()) return false;
        if (!validateSignature(accessKeyId, signature, allParams, timestampSec)) return false;
        bucket.set("1", authConfig.getSignature().getTimestampDriftSeconds(), TimeUnit.SECONDS);
        return true;
    }

    @Override
    public List<AccessKeyVO> listAccessKeys() {
        List<TranscodeAccessKey> list = accessKeyMapper.selectList(null);
        return list.stream().map(ak -> new AccessKeyVO(
                ak.getAccessKeyId(),
                ak.getName(),
                ak.getStatus(),
                ak.getDescription(),
                ak.getCreatedAt() != null ? ak.getCreatedAt().toString() : null
        )).collect(Collectors.toList());
    }

    @Override
    public boolean deleteAccessKey(String accessKeyId) {
        if (accessKeyId == null || accessKeyId.isBlank()) return false;
        return accessKeyMapper.delete(
                new LambdaQueryWrapper<TranscodeAccessKey>()
                        .eq(TranscodeAccessKey::getAccessKeyId, accessKeyId)) > 0;
    }
}
