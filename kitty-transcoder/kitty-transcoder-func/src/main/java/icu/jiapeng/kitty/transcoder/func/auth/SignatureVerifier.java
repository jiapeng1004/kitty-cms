package icu.jiapeng.kitty.transcoder.func.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 签名校验：参考 签名设计.doc，HMAC-SHA1，key = AccessKeySecret + "&"。
 */
public final class SignatureVerifier {

    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * 构造规范化请求字符串：参数按名字典序排序，URL 编码后 key=value&...
     */
    public static String buildCanonicalizedQueryString(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        return sorted.entrySet().stream()
                .map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * StringToSign = HTTPMethod + "&" + percentEncode("/") + "&" + percentEncode(CanonicalizedQueryString)
     */
    public static String buildStringToSign(String httpMethod, String canonicalizedQueryString) {
        return httpMethod + "&" + percentEncode("/") + "&" + percentEncode(canonicalizedQueryString);
    }

    /**
     * 使用 AccessKeySecret + "&" 作为 key，计算 HMAC-SHA1 后 Base64。
     */
    public static String computeSignature(String stringToSign, String accessKeySecret) throws NoSuchAlgorithmException, InvalidKeyException {
        String key = accessKeySecret + "&";
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
        byte[] hmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

    /**
     * RFC 3986 风格 URL 编码，空格为 %20。
     */
    public static String percentEncode(String value) {
        if (value == null) return "";
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }
}
