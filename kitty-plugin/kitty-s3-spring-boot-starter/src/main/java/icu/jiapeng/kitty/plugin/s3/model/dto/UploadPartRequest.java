package icu.jiapeng.kitty.plugin.s3.model.dto;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
public class UploadPartRequest {
    String uploadId;
    int partNumber;
    InputStream content;
    /** 声明长度；未知时可传 -1（由存储实现按流读完） */
    long size;
    /** 与 initiate 时 URL 中的桶名一致，用于校验会话 */
    String bucketName;
    /** 与 initiate 时 URL 中的对象键一致，用于校验会话 */
    String objectKey;
}
