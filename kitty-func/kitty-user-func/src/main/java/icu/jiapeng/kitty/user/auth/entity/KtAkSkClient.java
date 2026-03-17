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
package icu.jiapeng.kitty.user.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import icu.jiapeng.kitty.user.auth.enums.AkSkClientStatus;

@TableName("kt_aksk_client")
public class KtAkSkClient extends CommonEntity {
    /**
     * 客户端名字
     */
    @TableField("client_name")
    private String clientName;

    /**
     * AK
     */
    @TableField("access_key")
    private String accessKey;

    /**
     * SK
     */
    @TableField("secret_key")
    private String secretKey;

    /**
     * 客户端允许授权的scope集合
     */
    @TableField("allowed_scopes")
    private String allowedScopes;

    /**
     * 客户端状态
     * {@link AkSkClientStatus#getStatus()}
     */
    @TableField("status")
    private Integer status;
}