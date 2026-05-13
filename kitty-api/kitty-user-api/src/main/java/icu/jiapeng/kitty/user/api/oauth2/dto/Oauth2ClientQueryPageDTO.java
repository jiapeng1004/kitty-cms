package icu.jiapeng.kitty.user.api.oauth2.dto;

import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ClientVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "OAuth2 客户端分页查询参数")
public class Oauth2ClientQueryPageDTO extends PageReqDTO<Oauth2ClientVO> {

    @Schema(description = "关键词（客户端名称 / clientId）", nullable = true)
    private String searchKey;
}
