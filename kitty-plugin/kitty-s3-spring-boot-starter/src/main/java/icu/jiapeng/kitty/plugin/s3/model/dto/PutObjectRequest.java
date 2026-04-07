package icu.jiapeng.kitty.plugin.s3.model.dto;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;
import java.util.Map;

@Value
@Builder
public class PutObjectRequest {
    String bucketName;
    String key;
    InputStream content;
    long size;
    String contentType;
    Map<String, String> metadata;
}
