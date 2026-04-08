package icu.jiapeng.kitty.plugin.s3.model.dto;

import lombok.Builder;
import lombok.Value;

/**
 * 某次分片上传会话中已落盘的一片：协议层只依赖 {@link #partNumber} 与 {@link #storageKey}，
 * 具体键名格式由 {@link icu.jiapeng.kitty.plugin.s3.service.S3Service} 实现决定。
 */
@Value
@Builder
public class MultipartPartRecord {
    int partNumber;
    /** 对该存储后端执行 put/get/delete/compose 时使用的对象键 */
    String storageKey;
    long size;
    String eTag;
}
