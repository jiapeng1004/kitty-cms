package icu.jiapeng.qk.filter;

import icu.jiapeng.qk.config.AuthConfig;
import icu.jiapeng.qk.service.AccessKeyService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/** 全局签名鉴权：{@link ContainerRequestFilter}。 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SignatureAuthFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SignatureAuthFilter.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Inject
    AccessKeyService accessKeyService;

    @Inject
    AuthConfig authConfig;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getRequestUri().getPath();

        if (path.startsWith("/api/v1/auth") || path.equals("/health") || path.equals("/") || path.equals("/ping")) {
            return;
        }

        String ak = requestContext.getHeaderString("X-AK");
        String sign = requestContext.getHeaderString("X-SIGN");
        String timestampStr = requestContext.getHeaderString("X-TIMESTAMP");

        if (ak == null || sign == null || timestampStr == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"missing auth headers\"}")
                    .build());
            return;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long now = System.currentTimeMillis() / 1000;
            long drift = Math.abs(now - timestamp);

            if (drift > authConfig.interval()) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"timestamp drift too large\"}")
                        .build());
                return;
            }
        } catch (NumberFormatException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"invalid timestamp\"}")
                    .build());
            return;
        }

        String sk = accessKeyService.getSk(ak);
        if (sk == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"invalid ak\"}")
                    .build());
            return;
        }

        String expectedSign = computeSignature(sk, ak, timestampStr);
        if (!sign.equals(expectedSign)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"invalid signature\"}")
                    .build());
        }
    }

    private String computeSignature(String sk, String ak, String timestamp) {
        try {
            String message = ak + timestamp;
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(sk.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOG.error("Failed to compute signature", e);
            return null;
        }
    }
}
