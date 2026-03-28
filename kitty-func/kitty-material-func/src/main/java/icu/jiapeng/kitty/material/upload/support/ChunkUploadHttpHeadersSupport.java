package icu.jiapeng.kitty.material.upload.support;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;

/**
 * 分片上传请求头：解析 Content-Length、Content-Range（bytes），并做与报文体的一致性校验。
 * 参考 RFC 9110 / RFC 7233 常用写法：{@code bytes first-last/total}。
 */
public final class ChunkUploadHttpHeadersSupport {

    private ChunkUploadHttpHeadersSupport() {
    }

    /**
     * 解析 Content-Length，必须非负十进制整数。
     */
    public static long parseContentLengthLong(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        try {
            long v = Long.parseLong(headerValue.trim(), 10);
            if (v < 0) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            return v;
        } catch (NumberFormatException e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    /**
     * 解析 Content-Range：仅支持 {@code bytes first-last/completeLength}，且 {@code completeLength} 必须为确定值（不接受 {@code *}）。
     */
    public static ContentByteRange parseContentRangeBytes(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String h = headerValue.trim();
        if (h.length() < 5 || !h.regionMatches(true, 0, "bytes", 0, 5)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String rest = h.substring(5).trim();
        int slash = rest.indexOf('/');
        if (slash <= 0 || slash == rest.length() - 1) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        String rangePart = rest.substring(0, slash).trim();
        String totalPart = rest.substring(slash + 1).trim();
        if ("*".equals(totalPart)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long completeLength;
        try {
            completeLength = Long.parseLong(totalPart, 10);
        } catch (NumberFormatException e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (completeLength < 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        int dash = rangePart.indexOf('-');
        if (dash <= 0 || dash == rangePart.length() - 1) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long first;
        long last;
        try {
            first = Long.parseLong(rangePart.substring(0, dash).trim(), 10);
            last = Long.parseLong(rangePart.substring(dash + 1).trim(), 10);
        } catch (NumberFormatException e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return new ContentByteRange(first, last, completeLength);
    }

    /**
     * 校验区间与 Content-Length、实际 body 长度一致，且区间相对完整对象合法。
     */
    public static void validateRangeContentLengthAndBody(ContentByteRange range, long contentLength, int bodyLength) {
        if (bodyLength < 0) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (contentLength != bodyLength) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (range.firstBytePos() < 0 || range.lastBytePos() < range.firstBytePos()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (range.completeLength() > 0 && range.lastBytePos() >= range.completeLength()) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        long span;
        try {
            span = Math.addExact(Math.subtractExact(range.lastBytePos(), range.firstBytePos()), 1);
        } catch (ArithmeticException e) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (span != contentLength) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }
}
