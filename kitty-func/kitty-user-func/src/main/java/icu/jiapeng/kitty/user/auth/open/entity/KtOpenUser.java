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
package icu.jiapeng.kitty.user.auth.open.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 开放认证用户：上游 SSO 用户与本地用户的映射。
 * 用于根据 source + open_uid 快速查本地 user_id，并可扩展存储该 open 用户的其他属性。
 */
@Getter
@Setter
@TableName("kt_open_user")
public class KtOpenUser extends CommonEntity {

    /**
     * 平台标识：feishu、dingtalk、github、google、microsoft、wechat
     */
    @TableField("source")
    private String source;

    /**
     * 上游 SSO 用户唯一标识（第三方返回的 uuid/open_id 等）
     */
    @TableField("open_uid")
    private String openUid;

    /**
     * 本地用户 id，关联 kt_user_user.id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 扩展属性（JSON），如 nickname、avatar 等，便于后续按需存储
     */
    @TableField("extra")
    private String extra;
}
