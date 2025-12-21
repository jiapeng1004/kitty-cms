package icu.jiapeng.kitty.material.infrastructure.resource.repository;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.domain.resource.entity.MetaFile;
import icu.jiapeng.kitty.material.domain.resource.repository.MetaFileRepository;
import icu.jiapeng.kitty.material.infrastructure.repository.MybatisPlusBaseCrudRepository;
import icu.jiapeng.kitty.material.infrastructure.resource.convert.BeansConvert;
import icu.jiapeng.kitty.material.infrastructure.resource.entity.MpMetaFile;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Repository
public class MetaFileRepositoryImpl extends MybatisPlusBaseCrudRepository<MpMetaFile, MetaFile> implements MetaFileRepository {

    public MetaFileRepositoryImpl(IService<MpMetaFile> mybatisPlusService) {
        super(mybatisPlusService);
    }

    @Override
    public MpMetaFile domainEntity2MpEntity(MetaFile metaFile) {
        return BeansConvert.INSTANCE.metaFile2MpMetaFile(metaFile);
    }

    @Override
    public MetaFile mpEntity2DomainEntity(MpMetaFile metaFileEntity) {
        return BeansConvert.INSTANCE.mpMetaFile2MetaFile(metaFileEntity);
    }

    @Service
    public static class MetaFileEntityServiceImpl extends ServiceImpl<MetaFileRepositoryImpl.MetaFileEntityMapper, MpMetaFile> implements MetaFileRepositoryImpl.MetaFileEntityService {

    }

    public interface MetaFileEntityMapper extends BaseMapper<MpMetaFile> {

    }

    public interface MetaFileEntityService extends IService<MpMetaFile> {
    }
}

