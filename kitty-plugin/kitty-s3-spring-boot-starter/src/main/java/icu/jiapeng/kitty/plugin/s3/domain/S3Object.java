package icu.jiapeng.kitty.plugin.s3.domain;

import lombok.*;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * S3对象领域模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class S3Object {
    private String bucketName;
    private String key;
    private long size;
    private String contentType;
    private String eTag;
    private Date lastModified;
    private Map<String, String> metadata;
    private transient InputStream content;
}
