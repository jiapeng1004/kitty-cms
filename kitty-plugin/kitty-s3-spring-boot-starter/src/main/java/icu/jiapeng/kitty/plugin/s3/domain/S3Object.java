package icu.jiapeng.kitty.plugin.s3.domain;

import lombok.*;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

/**
 * S3 对象领域模型。
 *
 * <p>对象体通过 {@link #contentStreamSupplier} 懒打开：避免在服务层提前 {@code getObject} 打开流却未被消费导致泄漏；
 * 调用方在需要传输或读取时再 {@link #openContentStream()}，并在 try-with-resources 中关闭。
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

    /**
     * 懒打开对象体；每次 {@link Supplier#get()} 应返回新的 {@link InputStream}，由调用方关闭。
     * 不参与 JSON 持久化。
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private transient Supplier<InputStream> contentStreamSupplier;

    /**
     * 打开一次内容流；调用方负责在 {@code try}-with-resources 中关闭。
     *
     * @return 流；无 supplier 时为 {@code null}
     */
    public InputStream openContentStream() {
        return contentStreamSupplier == null ? null : contentStreamSupplier.get();
    }
}
