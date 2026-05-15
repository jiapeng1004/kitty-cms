package icu.jiapeng.kitty.user.api.role.api;

import icu.jiapeng.kitty.user.api.role.vo.KtRoleListInfoVo;
import icu.jiapeng.kitty.user.api.role.vo.RoleDetailInfoVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "kitty-user", contextId = "userInternalRole")
public interface KtRoleApi {
    @GetMapping("/api/role/detail/{roleId}")
    RoleDetailInfoVo detail(@NotBlank(message = "role.id.not.blank") @Schema(description = "角色id") @PathVariable String roleId);

    @GetMapping("/api/role/page")
    KtRoleListInfoVo page();
}
