package icu.jiapeng.kitty.material.storage;

import lombok.Data;

/**
 * 存储路由结果。
 */
@Data
public class StorageRouteResult {
    private String storageId;
    private String driverName;
    private String objectKey;
    private String routeTarget;
}
