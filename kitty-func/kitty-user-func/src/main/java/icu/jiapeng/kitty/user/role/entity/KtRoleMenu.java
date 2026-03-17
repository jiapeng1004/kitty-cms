package icu.jiapeng.kitty.user.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色-菜单关系表
 */
@TableName("kt_role_menu")
@Getter
@Setter
public class KtRoleMenu extends CommonEntity {

    /**
     * 角色 id
     */
    @TableField("role_id")
    private String roleId;

    /**
     * 菜单 id
     */
    @TableField("menu_id")
    private String menuId;
}

