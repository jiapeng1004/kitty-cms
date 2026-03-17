package icu.jiapeng.kitty.user.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.jiapeng.kitty.user.role.entity.KtRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关系表 Mapper
 */
@Mapper
public interface KtRoleMenuMapper extends BaseMapper<KtRoleMenu> {
}

