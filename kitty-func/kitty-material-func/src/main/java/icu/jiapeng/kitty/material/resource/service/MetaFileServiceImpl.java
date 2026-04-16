package icu.jiapeng.kitty.material.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.resource.entity.KtMetaFile;
import icu.jiapeng.kitty.material.resource.mapper.KtMetaFileMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class MetaFileServiceImpl extends ServiceImpl<KtMetaFileMapper, KtMetaFile> implements MetaFileService {

    @Override
    public Optional<KtMetaFile> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public List<KtMetaFile> findAll() {
        return list();
    }

    @Override
    public Optional<KtMetaFile> findByResourceId(String resourceId) {
        return list().stream()
                .filter(m -> resourceId.equals(m.getResourceId()))
                .findFirst();
    }

    @Override
    public List<KtMetaFile> listByResourceIds(Collection<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        return list(new QueryWrapper<KtMetaFile>().in("resource_id", resourceIds));
    }
}
