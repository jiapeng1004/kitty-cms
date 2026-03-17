package icu.jiapeng.kitty.user.oauth2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "OAuth2 客户端信息")
public class Oauth2ClientVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "客户端名称")
    private String clientName;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "允许的 scope，逗号分隔")
    private String allowedScopes;

    @Schema(description = "允许的授权类型，逗号分隔，参考 Oauth2GrantType")
    private String allowedGrantTypes;

    @Schema(description = "允许的认证方式，逗号分隔")
    private String allowAuthenticationMethods;

    @Schema(description = "允许的回调地址，逗号分隔")
    private String allowedRedirectUris;

    @Schema(description = "访问令牌有效期（秒）")
    private Long accessTokenTimeout;

    @Schema(description = "刷新令牌有效期（秒）")
    private Long refreshTokenTimeout;

    @Schema(description = "黑名单豁免标识")
    private String blackListExemption;

    @Schema(description = "状态：1启用，0禁用")
    private Integer status;

    @Schema(description = "是否需要授权确认：1需要，0不需要")
    private Integer requireAuthorizationConsent;

    @Schema(description = "创建时间")
    private Date createTime;
}
