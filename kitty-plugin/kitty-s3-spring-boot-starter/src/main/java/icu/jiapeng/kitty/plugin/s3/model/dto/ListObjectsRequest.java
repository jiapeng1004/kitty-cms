package icu.jiapeng.kitty.plugin.s3.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ListObjectsRequest {
    String bucketName;
    String prefix;
    String delimiter;
    int maxKeys;
    String marker;
}
