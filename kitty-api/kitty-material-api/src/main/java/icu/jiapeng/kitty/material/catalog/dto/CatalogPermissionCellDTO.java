package icu.jiapeng.kitty.material.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 单格授权：栏目 × 角色 × 权限码。
 */
@Schema(description = "栏目权限单格（授予或撤销）")
@Getter
@Setter
public class CatalogPermissionCellDTO {

    @NotBlank
    @Schema(description = "角色 ID")
    private String roleId;

    @NotBlank
    @Schema(description = "栏目 ID")
    private String catalogId;

    @NotBlank
    @Schema(description = "权限编码")
    private String permissionCode;
}
