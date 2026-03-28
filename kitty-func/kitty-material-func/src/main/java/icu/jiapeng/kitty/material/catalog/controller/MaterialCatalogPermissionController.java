package icu.jiapeng.kitty.material.catalog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionUpsertDTO;
import icu.jiapeng.kitty.material.catalog.service.CatalogPermissionService;
import icu.jiapeng.kitty.material.catalog.api.MaterialCatalogPermissionApi;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 栏目权限控制器。
 */
@RestController
@RequiredArgsConstructor
@Validated
public class MaterialCatalogPermissionController implements MaterialCatalogPermissionApi {
    private final CatalogPermissionService catalogPermissionService;
    private final UserContextGateway userContextGateway;

    @Operation(summary = "判断当前用户栏目上是否有指定权限")
    @GetMapping("/api/catalogPermission/check")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @Override
    public boolean checkPermission(String catalogId, String permissionCode) {
        List<String> roleIds = userContextGateway.currentRoleIds();
        return catalogPermissionService.hasPermission(catalogId, permissionCode, roleIds);
    }

    /**
     * 获取单角色/角色集合拥有栏目权限集合 Map<String, List<String>> 包含 栏目id-权限集合
     */
    @Operation(summary = "获取指定角色的权限树")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @PostMapping("/api/catalogPermission/map")
    @Override
    public Map<String, List<String>> getCatalogPermissions(@RequestBody List<String> roleIds) {
        return catalogPermissionService.getCatalogPermissions(roleIds);
    }

    /**
     * 获取单角色/角色集合拥有栏目权限集合 Map<String, List<String>> 包含 栏目id-权限集合
     */
    @Operation(summary = "获取当前用户的权限树")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @GetMapping("/api/catalogPermission/tree/currentUser")
    @Override
    public Map<String, List<String>> getCatalogPermissions() {
        List<String> roleIds = userContextGateway.currentRoleIds();
        return catalogPermissionService.getCatalogPermissions(roleIds);
    }

    /**
     * upsert更新全新
     */
    @Operation(summary = "更新栏目权限")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostMapping("/api/catalogPermission/upsert")
    @Override
    public void upsertCatalogPermission(@RequestBody @Valid CatalogPermissionUpsertDTO catalogPermissionUpsertDTO) {
        catalogPermissionService.upsert(catalogPermissionUpsertDTO);
    }

    @Operation(summary = "单格授权")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostMapping("/api/catalogPermission/grant")
    @Override
    public void grantCatalogPermission(@RequestBody @Valid CatalogPermissionCellDTO dto) {
        catalogPermissionService.grant(dto);
    }

    @Operation(summary = "单格撤销")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    @PostMapping("/api/catalogPermission/revoke")
    @Override
    public void revokeCatalogPermission(@RequestBody @Valid CatalogPermissionCellDTO dto) {
        catalogPermissionService.revoke(dto);
    }
}
