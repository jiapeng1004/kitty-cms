package icu.jiapeng.kitty.material.upload.support;

/**
 * 自 Content-Range 解析出的字节区间（含端点）与表示的完整对象长度。
 *
 * @param firstBytePos  起字节，从 0 开始
 * @param lastBytePos   止字节（含）
 * @param completeLength RFC7233 中的完整长度
 */
public record ContentByteRange(long firstBytePos, long lastBytePos, long completeLength) {
}
