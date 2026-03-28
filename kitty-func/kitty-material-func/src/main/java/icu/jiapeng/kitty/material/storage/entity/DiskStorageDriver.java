package icu.jiapeng.kitty.material.storage.entity;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageRouteRequest;
import icu.jiapeng.kitty.material.storage.StorageRouteResult;
import org.springframework.stereotype.Component;

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