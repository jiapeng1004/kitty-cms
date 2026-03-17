/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户属性表
 *
 */
@Getter
@Setter
@TableName("kt_user_attr")
public class KtUserAttr extends CommonEntity {
    /**
     * {@link KtUser#getId()}
     */
    @TableField("user_id")
    private String userId;

    /**
     * 属性key
     */
    @TableField("attr_key")
    private String attrKey;

    /**
     * 属性value
     */
    @TableField("attr_value")
    private String attrValue;
}
