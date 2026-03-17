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
package icu.jiapeng.kitty.user.tenant.convert;

import icu.jiapeng.kitty.user.tenant.dto.TenantCreateDTO;
import icu.jiapeng.kitty.user.tenant.dto.TenantUpdateDTO;
import icu.jiapeng.kitty.user.tenant.entity.KtTenant;
import icu.jiapeng.kitty.user.tenant.vo.TenantVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 租户转换器
 */
@Mapper
public interface TenantConvert {
    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    /**
     * 创建DTO转Entity
     */
    KtTenant createDtoToEntity(TenantCreateDTO dto);

    /**
     * 更新DTO转Entity
     */
    KtTenant updateDtoToEntity(TenantUpdateDTO dto);

    /**
     * Entity转VO
     */
    TenantVO entityToVo(KtTenant entity);
}