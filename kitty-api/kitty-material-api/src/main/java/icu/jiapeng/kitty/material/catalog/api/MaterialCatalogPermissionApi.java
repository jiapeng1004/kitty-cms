package icu.jiapeng.kitty.material.catalog.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionUpsertDTO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Material 栏目权限 API（OpenFeign + MVC 契约）。
 */
@Tag(name = "Material-栏目权限")
@FeignClient(name = "kitty-mam", contextId = "materialCatalogPermission")
public interface MaterialCatalogPermissionApi {

    @Operation(summary = "判断当前用户栏目上是否有指定权限")
    @GetMapping("/api/catalogPermission/check")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    boolean checkPermission(@RequestParam String catalogId, @RequestParam String permissionCode);

    @Operation(summary = "获取指定角色的权限树")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @PostMapping("/api/catalogPermission/map")
    Map<String, List<String>> getCatalogPermissions(@RequestBody List<String> roleIds);

    @Operation(summary = "获取当前用户的权限树")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @GetMapping("/api/catalogPermission/tree/currentUser")
    Map<String, List<String>> getCatalogPermissions();

    @Operation(summary = "栏目权限批量替换：roleIds 指定角色；permissionCodesReplace 非空时仅替换这些权限码的可编辑行，为空则清空这些角色下全部可编辑规则后再写入 permissionItems")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostMapping("/api/catalogPermission/upsert")
    void upsertCatalogPermission(@RequestBody @Valid CatalogPermissionUpsertDTO catalogPermissionUpsertDTO);

    @Operation(summary = "单格授权（栏目 × 角色 × 权限码）")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostMapping("/api/catalogPermission/grant")
    void grantCatalogPermission(@RequestBody @Valid CatalogPermissionCellDTO dto);

    @Operation(summary = "单格撤销（仅删除可编辑的规则行）")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostMapping("/api/catalogPermission/revoke")
    void revokeCatalogPermission(@RequestBody @Valid CatalogPermissionCellDTO dto);
}
