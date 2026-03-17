package icu.jiapeng.kitty.user.oauth2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.user.oauth2.entity.KtOauth2Scope;
import icu.jiapeng.kitty.user.oauth2.vo.Oauth2ScopeVO;

import java.util.List;

public interface KtOauth2ScopeService extends IService<KtOauth2Scope> {
    List<Oauth2ScopeVO> listAll();
}
