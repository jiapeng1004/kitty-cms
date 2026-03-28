package icu.jiapeng.kitty.material.catalog.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Material 栏目 API（MVC 契约，由 {@code kitty-material-func} Controller 实现）。
 */
@Tag(name = "Material-栏目")
public interface MaterialCatalogApi {

    @Operation(summary = "查询栏目树")
    @GetMapping("/api/catalog/tree")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
    List<CatalogNodeVO> queryTree();

    @Operation(summary = "查询指定角色栏目权限树")
    @GetMapping("/api/catalog/permission/tree/role/{roleId}")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_PERMISSION_VIEW)
    List<CatalogNodeVO> catalogTreeWithRolePermission(@PathVariable String roleId);


    @Operation(description = "新建栏目")
    @PostMapping("/api/catalog")
    @SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_CREATE)
    String createCatalog(@RequestBody CatalogCreateDTO catalogCreateDTO);
}
