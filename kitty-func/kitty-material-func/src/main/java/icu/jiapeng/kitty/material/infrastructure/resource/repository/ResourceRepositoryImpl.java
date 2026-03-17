package icu.jiapeng.kitty.material.infrastructure.resource.repository;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.domain.resource.entity.Resource;
import icu.jiapeng.kitty.material.domain.resource.repository.ResourceRepository;
import icu.jiapeng.kitty.material.infrastructure.repository.MybatisPlusBaseCrudRepository;
import icu.jiapeng.kitty.material.infrastructure.resource.convert.BeansConvert;
import icu.jiapeng.kitty.material.infrastructure.resource.entity.MpResource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Repository
public class ResourceRepositoryImpl extends MybatisPlusBaseCrudRepository<MpResource, Resource> implements ResourceRepository {

    public ResourceRepositoryImpl(IService<MpResource> mybatisPlusService) {
        super(mybatisPlusService);
    }

    @Override
    public MpResource domainEntity2MpEntity(Resource metaFile) {
        return BeansConvert.INSTANCE.resource2MpResource(metaFile);
    }

    @Override
    public Resource mpEntity2DomainEntity(MpResource mpResource) {
        return BeansConvert.INSTANCE.mpResource2Resource(mpResource);
    }

    @Service
    public static class ResourceServiceImpl extends ServiceImpl<ResourceRepositoryImpl.ResourceMapper, MpResource> implements ResourceRepositoryImpl.ResourceService {

    }

    public interface ResourceMapper extends BaseMapper<MpResource> {

    }

    public interface ResourceService extends IService<MpResource> {
    }
}

