package icu.jiapeng.kitty.plugin.s3.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表接口返回的单个对象摘要（与具体存储实现无关）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3ObjectInfo {
    private String key;
    private long size;
    private String eTag;
    private long lastModified;
}
