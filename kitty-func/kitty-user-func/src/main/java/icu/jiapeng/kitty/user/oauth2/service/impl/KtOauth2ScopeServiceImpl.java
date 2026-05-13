package icu.jiapeng.kitty.user.oauth2.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.user.oauth2.entity.KtOauth2Scope;
import icu.jiapeng.kitty.user.oauth2.mapper.KtOauth2ScopeMapper;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ScopeService;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ScopeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KtOauth2ScopeServiceImpl extends ServiceImpl<KtOauth2ScopeMapper, KtOauth2Scope> implements KtOauth2ScopeService {
    @Override
    public List<Oauth2ScopeVO> listAll() {
        return lambdaQuery()
                .orderByAsc(KtOauth2Scope::getScopeCode)
                .list()
                .stream()
                .map(item -> BeanUtil.copyProperties(item, Oauth2ScopeVO.class))
                .toList();
    }
}
