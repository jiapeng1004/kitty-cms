package icu.jiapeng.kitty.material.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.storage.dto.MaterialStorageRoutePreviewDTO;
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
}
