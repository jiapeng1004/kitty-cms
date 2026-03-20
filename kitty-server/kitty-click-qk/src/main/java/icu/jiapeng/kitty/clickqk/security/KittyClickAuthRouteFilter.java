package icu.jiapeng.kitty.clickqk.security;

import icu.jiapeng.kitty.clickqk.constants.Constant;
import icu.jiapeng.kitty.clickqk.constants.ErrType;
import icu.jiapeng.kitty.clickqk.service.impl.MongoAkSkAuthService;
import icu.jiapeng.kitty.clickqk.vo.ErrorResp;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import static icu.jiapeng.kitty.clickqk.security.SignatureUtil.*;

@Slf4j
@ApplicationScoped
public class KittyClickAuthRouteFilter {

    @Inject
    MongoAkSkAuthService authService;

    private void unauthorized(RoutingContext rc, String message) {
        String path = rc.request().path();
        rc.response().setStatusCode(401).putHeader("Content-Type", "application/json; charset=utf-8");
        // 为了让 controller 完全不关心鉴权，这里按具体接口返回匹配的响应体结构
        ErrorResp errorResp = ErrorResp.of(ErrType.UN_AUTHORIZED.getErrCode(), message);
        rc.response().end(JsonObject.mapFrom(errorResp).toString());
    }

    private void error(RoutingContext rc, String message) {
        String path = rc.request().path();
        rc.response().setStatusCode(ErrType.NORMAL_EXCEPTION.getErrCode()).putHeader("Content-Type", "application/json; charset=utf-8");
        // 为了让 controller 完全不关心鉴权，这里按具体接口返回匹配的响应体结构
        ErrorResp errorResp = ErrorResp.of(ErrType.NORMAL_EXCEPTION.getErrCode(), message);
        rc.response().end(JsonObject.mapFrom(errorResp).toString());
    }

    @RouteFilter(value = 1)
    void auth(RoutingContext rc) {
        String path = rc.request().path();
        if (path == null || !path.startsWith("/v1/")) {
            rc.next();
            return;
        }
        String ak = rc.request().getHeader(Constant.X_AK);
        String sign = rc.request().getHeader(Constant.X_SIGN);
        // timestamp header: 兼容 X-TS / X-Timestamp / X-TIMESTAMP
        String tsRaw = rc.request().getHeader(Constant.X_TS);
        Long timestamp;
        try {
            timestamp = tsRaw == null || tsRaw.isBlank() ? null : Long.parseLong(tsRaw.trim());
        } catch (Exception e) {
            timestamp = null;
        }
        if (ak == null || ak.isBlank() || sign == null || sign.isBlank() || timestamp == null) {
            unauthorized(rc, "missing/invalid auth header (ak/sign/timestamp)");
            return;
        }

        final long timestampMillis = timestamp;

        authService.findSkByAk(ak)
                .subscribe().with(sk -> {
                    if (sk == null || sk.isBlank()) {
                        unauthorized(rc, "invalid ak");
                        return;
                    }

                    String canonical = canonicalForHeaderAuth(ak, timestampMillis);
                    String expected = hmacSha256Hex(sk, canonical);
                    if (!equalsConstantTime(expected, sign)) {
                        unauthorized(rc, "invalid signature");
                        return;
                    }
                    rc.next();
                }, err -> {
                    log.warn("kitty-click-qk auth lookup failed", err);
                    error(rc, err.getMessage());
                });
    }
}

