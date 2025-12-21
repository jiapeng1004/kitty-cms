package icu.jiapeng.kitty.material.infrastructure.repository;


import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.domain.repository.BaseCrudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * mybatis plus 基础仓库
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@RequiredArgsConstructor
public abstract class MybatisPlusBaseCrudRepository<Entity, DomainEntity> implements BaseCrudRepository<DomainEntity> {
    private final IService<Entity> mybatisPlusService;


    public abstract Entity domainEntity2MpEntity(DomainEntity entity);

    public abstract DomainEntity mpEntity2DomainEntity(Entity entity);

    @Override
    public <S extends DomainEntity> boolean save(S entity) {
        return mybatisPlusService.save(domainEntity2MpEntity(entity));
    }

    @Override
    public <S extends DomainEntity> boolean saveAll(List<S> entities) {
        @SuppressWarnings("unchecked") Collection<Entity> collection = (Collection<Entity>) entities;
        return mybatisPlusService.saveBatch(collection);
    }

    @Override
    public Optional<DomainEntity> findById(String id) {
        Entity entity = mybatisPlusService.getById(id);
        return Optional.ofNullable(mpEntity2DomainEntity(entity));
    }

    @Override
    public boolean existsById(String id) {
        ResolvableType resolvableType = ResolvableType.forClass(this.getClass()).as(BaseCrudRepository.class);
        Class<?> entityClass = resolvableType.getGeneric(0).resolve();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo == null) {
            throw new RuntimeException("TableInfo is null");
        }
        String keyColumn = tableInfo.getKeyColumn();
        return mybatisPlusService.exists(mybatisPlusService.query().eq(keyColumn, id));
    }

    @Override
    public List<DomainEntity> findAll() {
        return mybatisPlusService.list().stream().map(this::mpEntity2DomainEntity).toList();
    }

    @Override
    public List<DomainEntity> findAllById(List<String> ids) {
        return mybatisPlusService.listByIds(ids).stream().map(this::mpEntity2DomainEntity).toList();
    }

    @Override
    public boolean deleteById(String id) {
        return mybatisPlusService.removeById(id);
    }

    @Override
    public boolean deleteAllById(List<String> ids) {
        return mybatisPlusService.removeByIds(ids);
    }
}
