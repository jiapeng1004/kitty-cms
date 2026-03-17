package icu.jiapeng.kitty.material.infrastructure.resource.convert;

import icu.jiapeng.kitty.material.domain.resource.entity.FileStorage;
import icu.jiapeng.kitty.material.domain.resource.entity.MetaFile;
import icu.jiapeng.kitty.material.domain.resource.entity.Resource;
import icu.jiapeng.kitty.material.infrastructure.resource.entity.MpFileStorage;
import icu.jiapeng.kitty.material.infrastructure.resource.entity.MpMetaFile;
import icu.jiapeng.kitty.material.infrastructure.resource.entity.MpResource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Mapper
public interface BeansConvert {
    BeansConvert INSTANCE = Mappers.getMapper(BeansConvert.class);


    // ==== 文件 ====
    MpMetaFile metaFile2MpMetaFile(MetaFile metaFile);

    MetaFile mpMetaFile2MetaFile(MpMetaFile mpMetaFile);


    // ==== 资源 ====
    MpResource resource2MpResource(Resource resource);

    Resource mpResource2Resource(MpResource mpResource);

    // ==== 文件存储器 ====
    MpFileStorage fileStorage2MpFileStorage(FileStorage fileStorage);

    FileStorage mpFileStorage2FileStorage(MpFileStorage mpFileStorage);
}
