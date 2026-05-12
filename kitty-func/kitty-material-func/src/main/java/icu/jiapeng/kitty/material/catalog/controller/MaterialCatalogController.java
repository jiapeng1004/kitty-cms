package icu.jiapeng.kitty.material.catalog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.api.MaterialCatalogApi;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogUpdateDTO;
import icu.jiapeng.kitty.material.catalog.service.CatalogService;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 栏目控制器：实现 API 契约并调用栏目业务服务。
 */
@RestController
@RequiredArgsConstructor
@Validated
public class MaterialCatalogController implements MaterialCatalogApi {


    private final CatalogService catalogService;

    @Operation(summary = "查询栏目树")
    @GetMapping("/api/catalog/tree")
    @Override
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    public List<CatalogNodeVO> queryTree() {
        return catalogService.catalogTreeWithUserPermission();
    }

    @Operation(summary = "查询指定角色栏目权限树")
    @GetMapping("/api/catalog/permission/tree/role/{roleId}")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    @Override
    public List<CatalogNodeVO> catalogTreeWithRolePermission(@PathVariable String roleId) {
        return catalogService.catalogTreeWithRolePermission(roleId);
    }

    @Operation(description = "新建栏目")
    @PostMapping("/api/catalog")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_CREATE)
    @Override
    public String createCatalog(@RequestBody CatalogCreateDTO catalogCreateDTO) {
        return catalogService.create(catalogCreateDTO);
    }

    @Operation(description = "更新栏目（重命名/移动/排序）")
    @PutMapping("/api/catalog")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    @Override
    public void updateCatalog(@RequestBody @Valid CatalogUpdateDTO dto) {
        catalogService.update(dto);
    }

    @Operation(description = "删除栏目")
    @DeleteMapping("/api/catalog/{catalogId}")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    @Override
    public void deleteCatalog(@PathVariable String catalogId) {
        catalogService.delete(catalogId);
    }
}
