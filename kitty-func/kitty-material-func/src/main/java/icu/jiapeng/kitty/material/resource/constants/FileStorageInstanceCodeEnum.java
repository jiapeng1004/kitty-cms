package icu.jiapeng.kitty.material.resource.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 存储实例编码（与 {@link FileEngineTypeEnum} 组合唯一），前后端约定一致，禁止随意手写。
 */
@AllArgsConstructor
@Getter
public enum FileStorageInstanceCodeEnum {

    /** S3 兼容 — 默认实例 */
    S3_DEFAULT(FileEngineTypeEnum.OBJECT_STORAGE.getType(), "default-s3", "默认对象存储"),

    /** S3 兼容 — MinIO / 自建开发环境 */
    S3_MINIO_DEV(FileEngineTypeEnum.OBJECT_STORAGE.getType(), "minio-dev", "MinIO 开发"),

    /** 本地磁盘 — 默认实例 */
    DISK_DEFAULT(FileEngineTypeEnum.DISK.getType(), "default-disk", "默认本地磁盘"),
    ;

    private final String storageType;

    /** 建议用作存储主键 id 的预设值（与引擎类型成对；仅文档/快捷输入，非库表强制） */
    private final String code;

    private final String label;

    public static boolean isValidPair(String storageType, String storageCode) {
        if (storageType == null || storageCode == null) {
            return false;
        }
        String t = storageType.trim();
        String c = storageCode.trim();
        return Arrays.stream(values())
                .anyMatch(e -> e.storageType.equals(t) && e.code.equals(c));
    }
}
