package icu.jiapeng.kitty.material.storage.api;

import icu.jiapeng.kitty.material.storage.dto.MaterialFileStorageUpsertDTO;
import icu.jiapeng.kitty.material.storage.dto.MaterialStorageRoutePreviewDTO;
import icu.jiapeng.kitty.material.storage.vo.MaterialFileStorageVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageInstanceOptionVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageConnectivityVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageObjectKeyNormalizeVO;
import icu.jiapeng.kitty.material.storage.vo.MaterialStorageRoutePreviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Material 存储 API（MVC 契约）。
 */
@Tag(name = "Material-存储")
public interface MaterialStorageApi {

    @Operation(summary = "存储列表")
    @GetMapping("/api/material/storage/list")
    List<String> listStorageIds();

    @Operation(summary = "存储路由预览")
    @PostMapping("/api/material/storage/route/preview")
    MaterialStorageRoutePreviewVO previewRoute(@Valid @RequestBody MaterialStorageRoutePreviewDTO req);

    @Operation(summary = "对象键规范化校验")
    @PostMapping("/api/material/storage/object-key/normalize")
    MaterialStorageObjectKeyNormalizeVO normalizeObjectKey(@Valid @RequestBody MaterialStorageRoutePreviewDTO req);

    @Operation(summary = "存储连通性检查")
    @PostMapping("/api/material/storage/connectivity/check")
    MaterialStorageConnectivityVO checkConnectivity(@Valid @RequestBody MaterialStorageRoutePreviewDTO req);

    @Operation(summary = "存储实例编码枚举（管理端下拉）")
    @GetMapping("/api/material/storage/config/instance-options")
    List<MaterialStorageInstanceOptionVO> listStorageInstanceOptions();

    @Operation(summary = "存储配置列表（管理）")
    @GetMapping("/api/material/storage/config/list")
    List<MaterialFileStorageVO> listFileStorageConfigs();

    @Operation(summary = "新建存储配置")
    @PostMapping("/api/material/storage/config")
    MaterialFileStorageVO createFileStorage(@Valid @RequestBody MaterialFileStorageUpsertDTO req);

    @Operation(summary = "更新存储配置")
    @PutMapping("/api/material/storage/config")
    MaterialFileStorageVO updateFileStorage(@Valid @RequestBody MaterialFileStorageUpsertDTO req);

    @Operation(summary = "删除存储配置")
    @DeleteMapping("/api/material/storage/config/{storageId}")
    void deleteFileStorage(@PathVariable("storageId") String storageId);
}
