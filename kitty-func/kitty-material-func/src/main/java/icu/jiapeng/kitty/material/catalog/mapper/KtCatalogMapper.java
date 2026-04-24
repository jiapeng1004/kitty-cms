package icu.jiapeng.kitty.material.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 栏目 Mapper。
 */
public interface KtCatalogMapper extends BaseMapper<KtCatalog> {

    /**
     * 同父下兄弟栏目，用于拖拽插位：稳定排序、避免仅靠 sort 为 null/并列时乱序。
     * <p>升序小在上：sort 升，其次 name、id</p>
     */
    List<KtCatalog> listByParentIdOrderBySort(@Param("parentId") String parentId);
    /**
     * 同父下 sort 的 min、max（null 作 0），单条结果 map：键 minV、maxV
     */
    Map<String, Object> selectMinMaxSortInParent(@Param("parentId") String parentId);
}
