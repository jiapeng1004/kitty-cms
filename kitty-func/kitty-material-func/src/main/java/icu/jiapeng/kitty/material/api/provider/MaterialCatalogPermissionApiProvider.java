package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.api.MaterialCatalogPermissionApi;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionCellDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogPermissionUpsertDTO;
import icu.jiapeng.kitty.material.catalog.service.CatalogPermissionService;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import icu.jiapeng.kitty.material.user.UserContextGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 与 {@link icu.jiapeng.kitty.material.catalog.controller.MaterialCatalogPermissionController} 同逻辑；
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialCatalogPermissionApiProvider implements MaterialCatalogPermissionApi {

    private final CatalogPermissionService catalogPermissionService;
    private final UserContextGateway userContextGateway;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    public boolean checkPermission(String catalogId, String permissionCode) {
        List<String> roleIds = userContextGateway.currentRoleIds();
        return catalogPermissionService.hasPermission(catalogId, permissionCode, roleIds);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    public Map<String, List<String>> getCatalogPermissions(List<String> roleIds) {
        return catalogPermissionService.getCatalogPermissions(roleIds);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    public Map<String, List<String>> getCatalogPermissions() {
        List<String> roleIds = userContextGateway.currentRoleIds();
        return catalogPermissionService.getCatalogPermissions(roleIds);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    public void upsertCatalogPermission(@Valid CatalogPermissionUpsertDTO catalogPermissionUpsertDTO) {
        catalogPermissionService.upsert(catalogPermissionUpsertDTO);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    public void grantCatalogPermission(@Valid CatalogPermissionCellDTO dto) {
        catalogPermissionService.grant(dto);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_EDIT)
    public void revokeCatalogPermission(@Valid CatalogPermissionCellDTO dto) {
        catalogPermissionService.revoke(dto);
    }
}
