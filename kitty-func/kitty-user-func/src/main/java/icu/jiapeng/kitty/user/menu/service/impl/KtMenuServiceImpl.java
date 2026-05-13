package icu.jiapeng.kitty.user.menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.user.menu.entity.KtMenu;
import icu.jiapeng.kitty.user.menu.mapper.KtMenuMapper;
import icu.jiapeng.kitty.user.menu.service.KtMenuService;
import icu.jiapeng.kitty.user.api.menu.vo.MenuTreeVo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KtMenuServiceImpl extends ServiceImpl<KtMenuMapper, KtMenu> implements KtMenuService {

    @Override
    public List<MenuTreeVo> getFullMenuTree() {
        List<KtMenu> all = lambdaQuery().orderByAsc(KtMenu::getSort).list();
        return buildTree(all, Collections.emptySet());
    }

    @Override
    public List<MenuTreeVo> getMenuTreeByPermissions(List<String> permissionCodes) {
        Set<String> permSet = permissionCodes == null
                ? Collections.emptySet()
                : new HashSet<>(permissionCodes);
        List<KtMenu> all = lambdaQuery()
                .eq(KtMenu::getEnabled, 1)
                .orderByAsc(KtMenu::getSort)
                .list();
        return buildTree(all, permSet);
    }

    private List<MenuTreeVo> buildTree(List<KtMenu> all, Set<String> permissionFilter) {
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<KtMenu>> parentGroup = all.stream()
                .collect(Collectors.groupingBy(m -> Optional.ofNullable(m.getParentId()).orElse("ROOT")));

        return buildChildren("ROOT", parentGroup, permissionFilter);
    }

    private List<MenuTreeVo> buildChildren(String parentId,
                                           Map<String, List<KtMenu>> parentGroup,
                                           Set<String> permissionFilter) {
        List<KtMenu> children = parentGroup.getOrDefault(parentId, Collections.emptyList());
        List<MenuTreeVo> result = new ArrayList<>();
        for (KtMenu menu : children) {
            // 权限过滤：若有绑定 p_codes 且当前用户一个都没有，则跳过
            if (!permissionFilter.isEmpty() && menu.getPCodes() != null && !menu.getPCodes().isEmpty()) {
                Set<String> need = Arrays.stream(menu.getPCodes().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
                if (Collections.disjoint(permissionFilter, need)) {
                    // 当前菜单对该用户不可见
                    continue;
                }
            }
            MenuTreeVo vo = new MenuTreeVo();
            vo.setId(menu.getId());
            vo.setParentId(menu.getParentId());
            vo.setMenuName(menu.getMenuName());
            vo.setMenuType(menu.getMenuType());
            vo.setMenuKey(menu.getMenuKey());
            vo.setPath(menu.getPath());
            vo.setComponent(menu.getComponent());
            vo.setLinkType(menu.getLinkType());
            vo.setLinkUrl(menu.getLinkUrl());
            vo.setIcon(menu.getIcon());
            vo.setSort(menu.getSort());
            vo.setPCodes(menu.getPCodes());
            vo.setChildren(buildChildren(menu.getId(), parentGroup, permissionFilter));
            result.add(vo);
        }
        return result;
    }
}

