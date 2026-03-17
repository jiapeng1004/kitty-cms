package icu.jiapeng.kitty.transcoder.func.grpc;

import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.RedisKeys;
import io.grpc.*;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

/**
 * gRPC 认证拦截器：支持 Token 或 AK/SK 签名。
 * - Token：metadata "authorization" = "Bearer {token}"，校验 Redis session 获取 accessKeyId
 * - 签名：metadata "x-access-key-id"、"x-signature"、"x-timestamp"、"x-signature-nonce"，校验签名
 */
//@Component
//@GlobalServerInterceptor
public class TranscoderGrpcAuthInterceptor implements ServerInterceptor {

    public static final Context.Key<String> ACCESS_KEY_ID_CTX = Context.key(TranscodeConstants.ATTR_ACCESS_KEY_ID);

    //    @Autowired
    private RedissonClient redissonClient;
    //    @Autowired
    private icu.jiapeng.kitty.transcoder.func.auth.AuthService authService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String methodName = call.getMethodDescriptor().getFullMethodName();
        if (methodName.startsWith("grpc.reflection")) {
            return next.startCall(call, headers);
        }

        String accessKeyId = null;

        String auth = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            if (!token.isEmpty() && authService.validateLoginToken(token)) {
                RMap<String, Object> session = redissonClient.getMap(RedisKeys.SESSION_KEY_PREFIX + token);
                Object ak = session.get("accessKeyId");
                if (ak != null) accessKeyId = ak.toString();
            }
        }

        if (accessKeyId == null) {
            String ak = headers.get(Metadata.Key.of("x-access-key-id", Metadata.ASCII_STRING_MARSHALLER));
            String sig = headers.get(Metadata.Key.of("x-signature", Metadata.ASCII_STRING_MARSHALLER));
            String ts = headers.get(Metadata.Key.of("x-timestamp", Metadata.ASCII_STRING_MARSHALLER));
            String nonce = headers.get(Metadata.Key.of("x-signature-nonce", Metadata.ASCII_STRING_MARSHALLER));
            if (ak != null && !ak.isEmpty() && sig != null && !sig.isEmpty() && ts != null && nonce != null) {
                long timestamp;
                try {
                    timestamp = Long.parseLong(ts.trim());
                } catch (NumberFormatException e) {
                    timestamp = 0;
                }
                Map<String, String> params = new HashMap<>();
                params.put("Method", methodName);
                params.put("Timestamp", ts);
                params.put("SignatureNonce", nonce);
                if (authService.validateSignatureAndConsumeNonce(ak, sig, nonce, timestamp, params)) {
                    accessKeyId = ak;
                }
            }
        }

        if (accessKeyId == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("需要 Token 或 AK/SK 签名"), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }

        Context ctx = Context.current().withValue(ACCESS_KEY_ID_CTX, accessKeyId);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
