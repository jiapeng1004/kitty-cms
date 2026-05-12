package icu.jiapeng.kitty.material.transcode.entity;

import icu.jiapeng.kitty.material.config.MaterialTranscodeProperties;
import icu.jiapeng.kitty.transcoder.api.MagicExtractFramesRequest;
import icu.jiapeng.kitty.transcoder.api.MagicImageConvertRequest;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * kitty-transcoder 魔法接口（同步抽帧/图转）HTTP 客户端，供封面、雪碧图等编排使用。
 * <p>
 * 需配置 {@link MaterialTranscodeProperties#getTranscoderHttpBase()}；认证与控制台 API 一致：
 * {@link MaterialTranscodeProperties#getTranscoderApiToken()}（Bearer）或 AK/SK 签名。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialTranscodeMagicClient {

    private static final String PATH_MAGIC_EXTRACT_FRAMES = "/api/transcode/magic/extract-frames";
    private static final String PATH_MAGIC_IMAGE_CONVERT = "/api/transcode/magic/image-convert";

    private static final String HMAC_SHA1 = "HmacSHA1";

    private final MaterialTranscodeProperties transcodeProperties;
    private final RestTemplate restTemplate;

    public TaskVO magicExtractFrames(MagicExtractFramesRequest req) {
        return postMagic(PATH_MAGIC_EXTRACT_FRAMES, req);
    }

    public TaskVO magicImageConvert(MagicImageConvertRequest req) {
        return postMagic(PATH_MAGIC_IMAGE_CONVERT, req);
    }

    private <T> TaskVO postMagic(String path, T body) {
        String base = transcodeProperties.getTranscoderHttpBase();
        if (!StringUtils.hasText(base)) {
            throw new IllegalStateException("未配置 kitty.material.transcode.transcoder-http-base，无法调用转码魔法 HTTP");
        }
        String url = joinBaseAndPath(base.trim(), path);
        String uriForSign = requestUriForSign(base.trim(), path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyTranscoderAuth(headers, "POST", uriForSign);
        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        TaskVO resp = restTemplate.postForObject(url, entity, TaskVO.class);
        if (resp == null) {
            throw new IllegalStateException("转码魔法 HTTP 返回空 body: " + url);
        }
        return resp;
    }

    private void applyTranscoderAuth(HttpHeaders headers, String method, String uriForSign) {
        if (StringUtils.hasText(transcodeProperties.getTranscoderApiToken())) {
            headers.setBearerAuth(transcodeProperties.getTranscoderApiToken().trim());
            return;
        }
        String ak = transcodeProperties.getTranscoderAccessKeyId();
        String sk = transcodeProperties.getTranscoderSecretKey();
        if (StringUtils.hasText(ak) && StringUtils.hasText(sk)) {
            long ts = System.currentTimeMillis() / 1000;
            String nonce = UUID.randomUUID().toString();
            Map<String, String> signBase = new HashMap<>();
            signBase.put("Method", method);
            signBase.put("Uri", uriForSign);
            signBase.put("Timestamp", String.valueOf(ts));
            signBase.put("SignatureNonce", nonce);
            String signature = signWithSecret(ak.trim(), sk.trim(), signBase, ts);
            headers.set("X-Access-Key-Id", ak.trim());
            headers.set("X-Timestamp", String.valueOf(ts));
            headers.set("X-Signature-Nonce", nonce);
            headers.set("X-Signature", signature);
            return;
        }
        log.debug("kitty-transcoder 魔法 HTTP 未配置 transcoder-api-token 或 AK/SK，若服务端开启 TokenAuthFilter 将返回 401");
    }

    /**
     * 与 kitty-transcoder 侧 HMAC-SHA1 签名算法一致（与控制台 HTTP API 相同）。
     */
    private static String signWithSecret(String accessKeyId, String secretKey, Map<String, String> params, long timestampSec) {
        Map<String, String> p = new HashMap<>(params);
        p.put("AccessKeyId", accessKeyId);
        p.put("Timestamp", String.valueOf(timestampSec));
        p.remove("Signature");
        String canonical = buildCanonicalizedQueryString(p);
        String httpMethod = p.get("Method");
        if (httpMethod == null || httpMethod.isBlank()) {
            httpMethod = "POST";
        }
        String stringToSign = buildStringToSign(httpMethod, canonical);
        try {
            return computeSignature(stringToSign, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("计算转码 HTTP 签名失败", e);
        }
    }

    private static String buildCanonicalizedQueryString(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        return sorted.entrySet().stream()
                .map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String buildStringToSign(String httpMethod, String canonicalizedQueryString) {
        return httpMethod + "&" + percentEncode("/") + "&" + percentEncode(canonicalizedQueryString);
    }

    private static String computeSignature(String stringToSign, String accessKeySecret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String key = accessKeySecret + "&";
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
        byte[] hmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

    private static String percentEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private static String joinBaseAndPath(String base, String path) {
        String b = base.replaceAll("/+$", "");
        String p = path.startsWith("/") ? path : "/" + path;
        return b + p;
    }

    /**
     * Servlet {@code request.getRequestURI()} 形态（含 context-path），用于 AK/SK 签名。
     */
    private static String requestUriForSign(String baseUrl, String apiPath) {
        try {
            URI u = URI.create(baseUrl);
            String contextPath = u.getPath();
            if (contextPath == null) {
                contextPath = "";
            }
            contextPath = contextPath.replaceAll("/+$", "");
            String p = apiPath.startsWith("/") ? apiPath : "/" + apiPath;
            if (contextPath.isEmpty()) {
                return p;
            }
            return contextPath + p;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("无效的 transcoderHttpBase: " + baseUrl, e);
        }
    }
}
