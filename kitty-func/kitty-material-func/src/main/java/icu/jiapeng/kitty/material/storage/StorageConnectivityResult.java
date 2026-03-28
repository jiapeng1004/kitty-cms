package icu.jiapeng.kitty.material.storage;

import lombok.Data;

/**
 * 存储连通性检测结果。
 */
@Data
public class StorageConnectivityResult {
    private String storageId;
    private Integer engineType;
    private String driverName;
    private Boolean reachable;
    private String detail;
}
