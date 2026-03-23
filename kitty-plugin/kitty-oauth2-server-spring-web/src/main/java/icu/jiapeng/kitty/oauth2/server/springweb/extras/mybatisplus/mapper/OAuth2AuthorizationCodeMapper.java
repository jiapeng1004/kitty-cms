package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity.OAuth2AuthorizationCodeRow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2AuthorizationCodeMapper extends BaseMapper<OAuth2AuthorizationCodeRow> {
}
