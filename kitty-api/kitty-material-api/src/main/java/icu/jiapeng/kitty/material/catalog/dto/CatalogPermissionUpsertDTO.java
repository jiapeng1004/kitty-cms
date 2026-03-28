package icu.jiapeng.kitty.material.catalog.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 栏目权限整包替换：先删除给定角色下全部「可编辑」规则，再按 permissionItems 写入。
 */
@Schema(description = "栏目权限全量替换（按角色批量）")
@Getter
@Setter
public class CatalogPermissionUpsertDTO {

    /**
     * 本次要清空并重写的角色集合（须与 permissionItems 中 roleId 一致）。
     */
    @NotEmpty
    @Schema(description = "要替换可编辑权限的角色 ID 列表")
    private List<String> roleIds;

    @Schema(description = "仅删除并重建这些权限码对应的可编辑行；为空表示删除这些角色下「全部」可编辑规则后再写入（慎用）")
    private List<String> permissionCodesReplace;

    @Valid
    @Schema(description = "授予矩阵（栏目 × 权限码）；可为空表示在已删除的范围内不再新增")
    private List<CatalogPermissionUpsertItemDTO> permissionItems;


    @Schema(description = "栏目权限矩阵一行")
    @Getter
    @Setter
    public static class CatalogPermissionUpsertItemDTO {
        @NotBlank
        @Schema(description = "角色 ID，须在 roleIds 内")
        private String roleId;

        @NotBlank
        @Schema(description = "栏目 ID")
        private String catalogId;

        @Schema(description = "该栏目在该角色下的权限编码列表")
        private List<String> permissionCodes;
    }
}
