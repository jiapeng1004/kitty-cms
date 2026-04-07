package icu.jiapeng.kitty.plugin.s3.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MultipartComposeResult {
    String eTag;
    long size;
}
