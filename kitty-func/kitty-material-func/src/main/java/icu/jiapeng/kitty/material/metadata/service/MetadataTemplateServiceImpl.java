package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplate;
import icu.jiapeng.kitty.material.metadata.mapper.KtMetadataTemplateMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MetadataTemplateServiceImpl extends ServiceImpl<KtMetadataTemplateMapper, KtMetadataTemplate> implements MetadataTemplateService {

    @Override
    public KtMetadataTemplate saveTemplate(KtMetadataTemplate entity) {
        saveOrUpdate(entity);
        return entity;
    }

    @Override
    public List<KtMetadataTemplate> saveAll(Iterable<KtMetadataTemplate> entities) {
        List<KtMetadataTemplate> list = new ArrayList<>();
        for (KtMetadataTemplate entity : entities) {
            saveOrUpdate(entity);
            list.add(entity);
        }
        return list;
    }

    @Override
    public Optional<KtMetadataTemplate> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public List<KtMetadataTemplate> findAll() {
        return list();
    }

    @Override
    public void delete(KtMetadataTemplate entity) {
        removeById(entity.getId());
    }

    @Override
    public void deleteAll(Iterable<? extends KtMetadataTemplate> entities) {
        for (KtMetadataTemplate entity : entities) {
            removeById(entity.getId());
        }
    }

    @Override
    public List<KtMetadataTemplate> listEnabledByCatalogAndType(String catalogId, Integer resourceType) {
        return list().stream()
                .filter(t -> t.getEnabled() != null && t.getEnabled() == 1)
                .filter(t -> catalogId.equals(t.getCatalogId()))
                .filter(t -> resourceType.equals(t.getResourceType()))
                .toList();
    }
}
