package icu.jiapeng.kitty.user.api.oauth2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OAuth2 客户端令牌端点认证方式")
public class Oauth2ClientAuthenticationMethodVO {

    @Schema(description = "协议侧 token_endpoint_auth_method 取值")
    private String code;

    @Schema(description = "显示名称")
    private String desc;
}
