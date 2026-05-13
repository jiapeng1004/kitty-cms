package icu.jiapeng.kitty.user.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.user.menu.entity.KtMenu;
import icu.jiapeng.kitty.user.api.menu.vo.MenuTreeVo;

import java.util.List;

public interface KtMenuService extends IService<KtMenu> {

    /**
     * 获取完整菜单树（不做权限过滤），用于菜单管理后台
     */
    List<MenuTreeVo> getFullMenuTree();

    /**
     * 根据权限 code 列表返回可见菜单树
     */
    List<MenuTreeVo> getMenuTreeByPermissions(List<String> permissionCodes);
}

