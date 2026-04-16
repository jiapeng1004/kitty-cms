package icu.jiapeng.kitty.material.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.storage.dto.MaterialFileStorageUpsertDTO;
import icu.jiapeng.kitty.material.storage.dto.MaterialStorageRoutePreviewDTO;
import icu.jiapeng.kitty.material.storage.vo.MaterialFileStorageVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageInstanceOptionVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageConnectivityVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageObjectKeyNormalizeVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageRoutePreviewVO;

import java.util.List;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/28
 */
public interface KtFileStorageService extends IService<KtFileStorage> {
    List<String> listStorageIds();

    MaterialStorageRoutePreviewVO previewRoute(MaterialStorageRoutePreviewDTO req);

    MaterialStorageObjectKeyNormalizeVO normalizeObjectKey(MaterialStorageRoutePreviewDTO req);

    MaterialStorageConnectivityVO checkConnectivity(MaterialStorageRoutePreviewDTO req);

    List<MaterialStorageInstanceOptionVO> listStorageInstanceOptions();

    List<MaterialFileStorageVO> listFileStorageConfigs();

    MaterialFileStorageVO createFileStorage(MaterialFileStorageUpsertDTO req);

    MaterialFileStorageVO updateFileStorage(MaterialFileStorageUpsertDTO req);

    void deleteFileStorage(String storageId);

    /** 当前主存储记录 ID；未设置主存储时抛出 MATERIAL_PRIMARY_STORAGE_NOT_SET。 */
    String requirePrimaryStorageId();
}
