package icu.jiapeng.kitty.user.api.oauth2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OAuth2 scope 信息")
public class Oauth2ScopeVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "scope 名称")
    private String scopeName;

    @Schema(description = "scope 编码")
    private String scopeCode;

    @Schema(description = "scope 描述")
    private String scopeDesc;
}
