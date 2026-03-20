package icu.jiapeng.kitty.clickqk.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class SignatureUtil {

    private SignatureUtil() {
    }

    public static String hmacSha256Hex(String secret, String canonicalString) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(canonicalString.getBytes(StandardCharsets.UTF_8));
            // 手写 hex，避免引入额外依赖
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("failed to calculate hmac sha256", e);
        }
    }

    public static boolean equalsConstantTime(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Header-based auth canonical string：只依赖 ak + timestamp。
     *
     * HMAC key：sk
     * HMAC data：canonicalForHeaderAuth(ak, timestamp)
     */
    public static String canonicalForHeaderAuth(String ak, long timestamp) {
        return ak + "|" + timestamp;
    }

    /**
     * 统一签名串格式，客户端与服务端必须完全一致。
     */
    public static String canonicalForReport(
            String ak,
            String bizType,
            String eventType,
            String userId,
            long eventTsMillis,
            long value
    ) {
        return ak + "|" + bizType + "|" + eventType + "|" + (userId == null ? "" : userId) + "|" + eventTsMillis + "|" + value;
    }

    public static String canonicalForStats(
            String ak,
            String bizType,
            String interval,
            long fromMillis,
            long toMillis
    ) {
        return ak + "|" + bizType + "|" + interval + "|" + fromMillis + "|" + toMillis;
    }

    static void main() {
        String s = canonicalForHeaderAuth("root", 123456);
        String s1 = hmacSha256Hex("123456", s);
        System.out.println("2961db494c52e85381ad25e6e6761a3ccf57e2848dbe0de24684fe9159829aeb");
        System.out.println(s1);
    }
}

