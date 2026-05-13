package icu.jiapeng.kitty.user.api.menu.api;

import icu.jiapeng.kitty.user.api.menu.vo.MenuTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "kitty-user", contextId = "userInternalConfig")
public interface KtMenuApi {
    @GetMapping("/api/menu/tree/all")
    @Operation(summary = "获取完整菜单树")
    List<MenuTreeVo> allTree();

    @GetMapping("/api/menu/tree/current")
    @Operation(summary = "获取当前用户可见菜单树")
    List<MenuTreeVo> currentUserTree();
}
