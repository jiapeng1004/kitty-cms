package icu.jiapeng.kitty.material.api.provider;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.api.MaterialCatalogApi;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogUpdateDTO;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 聚合单体等：与 {@link icu.jiapeng.kitty.material.catalog.controller.MaterialCatalogController} 同逻辑，
 * 作为本地 {@link MaterialCatalogApi} 实现（不注册 MVC 映射）。
 * 与 Controller 通过 profile {@code kitty-mam-embedded-api} 互斥。
 */
@Primary
@Service
@RequiredArgsConstructor
@Validated
public class MaterialCatalogApiProvider implements MaterialCatalogApi {

    private final CatalogService catalogService;

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    public List<CatalogNodeVO> queryTree() {
        return catalogService.catalogTreeWithUserPermission();
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    public List<CatalogNodeVO> catalogTreeWithRolePermission(String roleId) {
        return catalogService.catalogTreeWithRolePermission(roleId);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_CREATE)
    public String createCatalog(CatalogCreateDTO catalogCreateDTO) {
        return catalogService.create(catalogCreateDTO);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    public void updateCatalog(@Valid CatalogUpdateDTO dto) {
        catalogService.update(dto);
    }

    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    public void deleteCatalog(String catalogId) {
        catalogService.delete(catalogId);
    }
}
