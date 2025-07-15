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
import icu.jiapeng.kitty.user.auth.enums.Oauth2ClientStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 描述: 用户授权客户端
 */
@Getter
@Setter
@TableName("kt_user_oauth2_client")
public class KtUserOauth2Client extends CommonEntity {
    /**
     * 客户端名字
     */
    @TableField("client_name")
    private String clientName;

    /**
     * clientId
     */
    @TableField("client_id")
    private String clientId;

    /**
     * clientSecret
     */
    @TableField("client_secret")
    private String clientSecret;

    /**
     * 客户端允许授权的scope集合
     */
    @TableField("allowed_scopes")
    private String allowedScopes;

    /**
     * 允许的授权类型
     */
    @TableField("allowed_grant_types")
    private String allowedGrantTypes;

    /**
     * 允许的认证方式
     */
    @TableField("allow_authentication_methods")
    private String allowAuthenticationMethods;

    /**
     * 允许的回调地址
     */
    @TableField("allowed_redirect_uris")
    private String allowedRedirectUris;

    /**
     * 访问令牌有效期(秒)
     */
    @TableField("access_token_timeout")
    private Long accessTokenTimeout;

    /**
     * 刷新令牌有效期(秒)
     */
    @TableField("refresh_token_timeout")
    private Long refreshTokenTimeout;

    /**
     * 黑名单豁免flag
     */
    @TableField("black_list_exemption")
    private String blackListExemption;

    /**
     * 客户端状态
     * {@link Oauth2ClientStatus#getStatus()}
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否需要授权确认 1:需要
     */
    @TableField("require_authorization_consent")
    private Integer requireAuthorizationConsent;
}
