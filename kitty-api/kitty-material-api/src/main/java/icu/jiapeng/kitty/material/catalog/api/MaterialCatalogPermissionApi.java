package icu.jiapeng.kitty.material.catalog.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionUpsertDTO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * Material 栏目权限 API（MVC 契约）。
 */
@Tag(name = "Material-栏目权限")
public interface MaterialCatalogPermissionApi {

    @Operation(summary = "判断当前用户栏目上是否有指定权限")
    @GetExchange("/api/catalogPermission/check")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    boolean checkPermission(String catalogId, String permissionCode);

    @Operation(summary = "获取指定角色的权限树")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @PostExchange("/api/catalogPermission/map")
    Map<String, List<String>> getCatalogPermissions(@RequestBody List<String> roleIds);

    @Operation(summary = "获取当前用户的权限树")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @GetExchange("/api/catalogPermission/tree/currentUser")
    Map<String, List<String>> getCatalogPermissions();

    @Operation(summary = "栏目权限批量替换：roleIds 指定角色；permissionCodesReplace 非空时仅替换这些权限码的可编辑行，为空则清空这些角色下全部可编辑规则后再写入 permissionItems")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostExchange("/api/catalogPermission/upsert")
    void upsertCatalogPermission(@RequestBody @Valid CatalogPermissionUpsertDTO catalogPermissionUpsertDTO);

    @Operation(summary = "单格授权（栏目 × 角色 × 权限码）")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostExchange("/api/catalogPermission/grant")
    void grantCatalogPermission(@RequestBody @Valid CatalogPermissionCellDTO dto);

    @Operation(summary = "单格撤销（仅删除可编辑的规则行）")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostExchange("/api/catalogPermission/revoke")
    void revokeCatalogPermission(@RequestBody @Valid CatalogPermissionCellDTO dto);
}
