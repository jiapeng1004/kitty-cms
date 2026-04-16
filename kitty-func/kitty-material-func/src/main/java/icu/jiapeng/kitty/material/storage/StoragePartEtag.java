package icu.jiapeng.kitty.material.storage;

/**
 * S3 CompleteMultipartUpload 所需的 partNumber 与 ETag（不含引号）。
 */
public record StoragePartEtag(int partNumber, String eTag) {
}
