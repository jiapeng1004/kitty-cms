package icu.jiapeng.kitty.material.resource.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import icu.jiapeng.kitty.material.resource.entity.KtResource;
import org.apache.ibatis.annotations.Param;

/**
 * 素材 Mapper。
 * <p>
 * 不经过 {@link BaseMapper#selectPage} 的默认行为，由调用方在 {@code ew} 中组条件（如回收站需显式 {@code deleted = 1}），
 * 与 {@code Wrapper#getCustomSqlSegment} 文档一致。
 */
public interface KtResourceMapper extends BaseMapper<KtResource> {

}
