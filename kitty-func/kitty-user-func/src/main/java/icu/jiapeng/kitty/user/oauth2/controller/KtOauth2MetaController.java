package icu.jiapeng.kitty.user.oauth2.controller;

import icu.jiapeng.kitty.user.oauth2.api.KtOauth2MetaApi;
import icu.jiapeng.kitty.user.oauth2.enums.Oauth2GrantType;
import icu.jiapeng.kitty.user.oauth2.vo.Oauth2GrantTypeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class KtOauth2MetaController implements KtOauth2MetaApi {

    @Override
    @GetMapping("/open/oauth2/grant-types")
    public List<Oauth2GrantTypeVO> grantTypes() {
        return Arrays.stream(Oauth2GrantType.values())
                .map(item -> {
                    Oauth2GrantTypeVO vo = new Oauth2GrantTypeVO();
                    vo.setCode(item.getCode());
                    vo.setDesc(item.getDesc());
                    return vo;
                })
                .toList();
    }
}
