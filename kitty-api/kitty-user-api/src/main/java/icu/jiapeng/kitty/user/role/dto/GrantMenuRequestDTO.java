package icu.jiapeng.kitty.user.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 为角色开通菜单（主接口）请求 DTO
 */
@Data
public class GrantMenuRequestDTO {

    @Schema(description = "角色ID")
    @NotBlank(message = "role.id.not.blank")
    private String roleId;

    @Schema(description = "菜单ID")
    @NotBlank(message = "menu.id.not.blank")
    private String menuId;

    @Schema(description = "勾选的权限code列表（前端展示菜单p_codes并默认全选，允许用户取消后回传）")
    private List<String> pCodes;
}

