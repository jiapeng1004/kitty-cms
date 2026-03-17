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
package icu.jiapeng.kitty.user.cfg.convert;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import icu.jiapeng.kitty.user.cfg.entity.KtConfig;
import icu.jiapeng.kitty.user.cfg.entity.KtConfigClass;
import icu.jiapeng.kitty.user.cfg.vo.ConfigClassListVo;
import icu.jiapeng.kitty.user.cfg.vo.ConfigListVo;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;


/**
 *
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Mapper
public interface BeansConvert {
    BeansConvert INSTANCE = Mappers.getMapper(BeansConvert.class);

    @Mapping(target = "configEnum", ignore = true)
    ConfigListVo config2ListVo(KtConfig ktConfig);

    @AfterMapping
    default void afterConfig2ListVo(@MappingTarget ConfigListVo configListVo, KtConfig ktConfig) {
        if (StringUtils.hasText(ktConfig.getConfigEnum())) {
            configListVo.setConfigEnum(JSONObject.parseObject(ktConfig.getConfigEnum(), new TypeReference<>() {
            }));
        }
    }

    ConfigClassListVo class2ListVo(KtConfigClass ktConfigClass);
}
