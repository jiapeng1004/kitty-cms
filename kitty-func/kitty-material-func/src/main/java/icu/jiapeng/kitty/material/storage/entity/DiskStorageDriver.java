package icu.jiapeng.kitty.material.storage.entity;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageRouteRequest;
import icu.jiapeng.kitty.material.storage.StorageRouteResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 磁盘存储驱动（路由契约实现）。
 */
@Component
public class DiskStorageDriver implements StorageDriver {

    @Override
    public String driverName() {
        return FileEngineTypeEnum.DISK.getType();
    }

    @Override
    public void putEmptyObject(KtFileStorage storageConfig, String objectKey, String contentTypeOrNull) throws IOException {
        Path target = resolveDiskObjectPath(storageConfig.getBucket(), objectKey);
        Files.createDirectories(target.getParent());
        Files.write(target, new byte[0]);
    }

    @Override
    public void writeSequentialLocalPartFilesToObject(KtFileStorage storageConfig, String objectKey, List<Path> orderedLocalPartPaths) throws IOException {
        Path target = resolveDiskObjectPath(storageConfig.getBucket(), objectKey);
        Files.createDirectories(target.getParent());
        try (OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (Path p : orderedLocalPartPaths) {
                Files.copy(p, out);
            }
        }
    }

    /**
     * 挂载根（bucket 字段）+ 对象键 → 本地绝对路径。
     */
    private Path resolveDiskObjectPath(String storageMount, String objectKey) throws IOException {
        if (storageMount == null || storageMount.isBlank()) {
            throw new IOException("disk storage mount (bucket) is blank");
        }
        Path t = Path.of(storageMount.trim());
        if (objectKey != null) {
            for (String seg : objectKey.split("/")) {
                if (!seg.isEmpty()) {
                    t = t.resolve(seg);
                }
            }
        }
        return t;
    }

    @Override
    public StorageRouteResult route(StorageRouteRequest request, KtFileStorage storageConfig) {
        String normalized = normalizeObjectKey(request.getObjectKey());
        StorageRouteResult result = new StorageRouteResult();
        result.setStorageId(request.getStorageId());
        result.setDriverName(driverName());
        result.setObjectKey(normalized);
        String configPart = StrUtil.firstNonBlank(storageConfig.getExternalEndpoint(), storageConfig.getInternalEndpoint());
        String path = PathUtil.builderPath(configPart, normalized);
        result.setRouteTarget("disk://" + path);
        return result;
    }

    @Override
    public String normalizeObjectKey(String objectKey) {
        if (objectKey == null) {
            return "";
        }
        String normalized = objectKey.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        normalized = normalized.replaceAll("/+", "/");
        return normalized;
    }

    @Override
    public boolean isValidObjectKey(String objectKey) {
        String normalized = normalizeObjectKey(objectKey);
        if (normalized.isBlank()) {
            return false;
        }
        return !normalized.contains("..") && !normalized.contains(":") && !normalized.startsWith(".");
    }
}
