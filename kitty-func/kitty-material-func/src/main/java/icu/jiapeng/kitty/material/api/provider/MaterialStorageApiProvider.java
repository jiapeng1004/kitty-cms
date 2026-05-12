package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.storage.api.MaterialStorageApi;
import icu.jiapeng.kitty.material.storage.dto.MaterialFileStorageUpsertDTO;
import icu.jiapeng.kitty.material.storage.dto.MaterialStorageRoutePreviewDTO;
import icu.jiapeng.kitty.material.storage.service.KtFileStorageService;
import icu.jiapeng.kitty.material.storage.vo.MaterialFileStorageVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageConnectivityVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageInstanceOptionVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageObjectKeyNormalizeVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageRoutePreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 与 {@link icu.jiapeng.kitty.material.storage.controller.MaterialStorageController} 同逻辑；
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialStorageApiProvider implements MaterialStorageApi {

    private final KtFileStorageService storageService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public List<String> listStorageIds() {
        return storageService.listStorageIds();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialStorageRoutePreviewVO previewRoute(MaterialStorageRoutePreviewDTO req) {
        return storageService.previewRoute(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialStorageObjectKeyNormalizeVO normalizeObjectKey(MaterialStorageRoutePreviewDTO req) {
        return storageService.normalizeObjectKey(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW)
    public MaterialStorageConnectivityVO checkConnectivity(MaterialStorageRoutePreviewDTO req) {
        return storageService.checkConnectivity(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_STORAGE_MANAGE)
    public List<MaterialStorageInstanceOptionVO> listStorageInstanceOptions() {
        return storageService.listStorageInstanceOptions();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_STORAGE_MANAGE)
    public List<MaterialFileStorageVO> listFileStorageConfigs() {
        return storageService.listFileStorageConfigs();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_STORAGE_MANAGE)
    public MaterialFileStorageVO createFileStorage(MaterialFileStorageUpsertDTO req) {
        return storageService.createFileStorage(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_STORAGE_MANAGE)
    public MaterialFileStorageVO updateFileStorage(MaterialFileStorageUpsertDTO req) {
        return storageService.updateFileStorage(req);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_STORAGE_MANAGE)
    public void deleteFileStorage(String storageId) {
        storageService.deleteFileStorage(storageId);
    }
}
