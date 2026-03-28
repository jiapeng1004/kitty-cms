package icu.jiapeng.kitty.material.storage;

import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;

/**
 * 存储驱动端口。
 */
public interface StorageDriver {

    String driverName();

    StorageRouteResult route(StorageRouteRequest request, KtFileStorage storageConfig);

    default String normalizeObjectKey(String objectKey) {
        return objectKey == null ? "" : objectKey.trim();
    }

    default boolean isValidObjectKey(String objectKey) {
        return objectKey != null && !objectKey.isBlank();
    }

    default StorageConnectivityResult testConnectivity(String storageId, KtFileStorage storageConfig) {
        StorageConnectivityResult result = new StorageConnectivityResult();
        result.setStorageId(storageId);
        result.setDriverName(driverName());
        result.setReachable(true);
        result.setDetail("ok");
        return result;
    }
}
