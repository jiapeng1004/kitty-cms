package icu.jiapeng.kitty.user.api.oauth2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OAuth2 授权类型信息")
public class Oauth2GrantTypeVO {

    @Schema(description = "协议侧 grant_type 字符串")
    private String code;

    @Schema(description = "显示名称")
    private String desc;
}
