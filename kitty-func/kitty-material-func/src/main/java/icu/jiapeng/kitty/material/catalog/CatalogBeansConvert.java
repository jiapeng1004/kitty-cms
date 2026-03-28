package icu.jiapeng.kitty.material.catalog;

import icu.jiapeng.kitty.material.catalog.dto.CatalogCreateDTO;
import icu.jiapeng.kitty.material.catalog.entity.KtCatalog;
import icu.jiapeng.kitty.material.catalog.vo.CatalogNodeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 栏目对象转换。
 */
@Mapper
public interface CatalogBeansConvert {

    CatalogBeansConvert INSTANCE = Mappers.getMapper(CatalogBeansConvert.class);

    KtCatalog node2Mp(CatalogCreateDTO node);

    CatalogNodeVO mp2Node(KtCatalog KtCatalog);
}
