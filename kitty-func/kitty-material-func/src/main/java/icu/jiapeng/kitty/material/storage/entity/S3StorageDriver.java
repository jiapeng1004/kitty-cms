package icu.jiapeng.kitty.material.storage.entity;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.storage.StorageConnectivityResult;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageRouteRequest;
import icu.jiapeng.kitty.material.storage.StorageRouteResult;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * S3 存储驱动（路由契约实现）。
 */
@Component
public class S3StorageDriver implements StorageDriver {

    @Override
    public String driverName() {
        return FileEngineTypeEnum.OBJECT_STORAGE.getType();
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
        result.setRouteTarget("s3://" + path);
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
        return normalized.replaceAll("/+", "/");
    }

    @Override
    public StorageConnectivityResult testConnectivity(String storageId, KtFileStorage storageConfig) {
        StorageConnectivityResult result = new StorageConnectivityResult();
        result.setStorageId(storageId);
        result.setDriverName(driverName());
        String endpoint = storageConfig.getInternalEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            result.setReachable(false);
            result.setDetail("missing endpoint in storage config");
            return result;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            result.setReachable(code >= 200 && code < 500);
            result.setDetail("endpoint=" + endpoint + ", httpStatus=" + code);
            return result;
        } catch (Exception e) {
            result.setReachable(false);
            result.setDetail("endpoint=" + endpoint + ", error=" + e.getClass().getSimpleName());
            return result;
        }
    }
}