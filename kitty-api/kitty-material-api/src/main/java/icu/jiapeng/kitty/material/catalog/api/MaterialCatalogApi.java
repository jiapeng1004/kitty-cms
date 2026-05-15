package icu.jiapeng.kitty.material.catalog.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.dto.CatalogUpdateDTO;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Material 栏目 API（OpenFeign + MVC 契约，由 {@code kitty-material-func} Controller 实现）。
 */
@Tag(name = "Material-栏目")
@FeignClient(name = "kitty-mam", contextId = "materialCatalog")
public interface MaterialCatalogApi {

    @Operation(summary = "查询栏目树")
    @GetMapping("/api/material/catalog/tree")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    List<CatalogNodeVO> queryTree();

    @Operation(summary = "查询指定角色栏目权限树")
    @GetMapping("/api/material/catalog/permission/tree/role/{roleId}")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    List<CatalogNodeVO> catalogTreeWithRolePermission(@PathVariable String roleId);


    @Operation(description = "新建栏目")
    @PostMapping("/api/material/catalog")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_CREATE)
    String createCatalog(@RequestBody CatalogCreateDTO catalogCreateDTO);

    @Operation(description = "更新栏目（重命名/移动/排序）")
    @PutMapping("/api/material/catalog")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    void updateCatalog(@RequestBody @Valid CatalogUpdateDTO dto);

    @Operation(description = "删除栏目")
    @DeleteMapping("/api/material/catalog/{catalogId}")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    void deleteCatalog(@PathVariable String catalogId);
}
