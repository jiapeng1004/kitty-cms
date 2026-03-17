package icu.jiapeng.kitty.material.infrastructure.resource.repository;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.domain.resource.entity.FileStorage;
import icu.jiapeng.kitty.material.domain.resource.repository.FileStorageRepository;
import icu.jiapeng.kitty.material.infrastructure.repository.MybatisPlusBaseCrudRepository;
import icu.jiapeng.kitty.material.infrastructure.resource.convert.BeansConvert;
import icu.jiapeng.kitty.material.infrastructure.resource.entity.MpFileStorage;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Repository
public class FileStorageRepositoryImpl extends MybatisPlusBaseCrudRepository<MpFileStorage, FileStorage> implements FileStorageRepository {
    public FileStorageRepositoryImpl(IService<MpFileStorage> mybatisPlusService) {
        super(mybatisPlusService);
    }

    @Override
    public MpFileStorage domainEntity2MpEntity(FileStorage fileStorage) {
        return BeansConvert.INSTANCE.fileStorage2MpFileStorage(fileStorage);
    }

    @Override
    public FileStorage mpEntity2DomainEntity(MpFileStorage mpFileStorage) {
        return BeansConvert.INSTANCE.mpFileStorage2FileStorage(mpFileStorage);
    }

    public interface MpFileStorageService extends IService<MpFileStorage> {
    }

    @Service
    public static class MpFileStorageServiceImpl extends ServiceImpl<MpFileStorageMapper, MpFileStorage> implements MpFileStorageService {
    }

    public interface MpFileStorageMapper extends BaseMapper<MpFileStorage> {
    }
}
