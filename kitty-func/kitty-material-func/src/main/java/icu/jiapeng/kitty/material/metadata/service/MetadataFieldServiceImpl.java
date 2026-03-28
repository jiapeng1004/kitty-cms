package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataField;
import icu.jiapeng.kitty.material.metadata.mapper.KtMetadataFieldMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MetadataFieldServiceImpl extends ServiceImpl<KtMetadataFieldMapper, KtMetadataField> implements MetadataFieldService {

    @Override
    public Optional<KtMetadataField> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public KtMetadataField saveField(KtMetadataField entity) {
        saveOrUpdate(entity);
        return entity;
    }

    @Override
    public List<KtMetadataField> saveAll(Iterable<KtMetadataField> entities) {
        List<KtMetadataField> list = new ArrayList<>();
        for (KtMetadataField entity : entities) {
            saveOrUpdate(entity);
            list.add(entity);
        }
        return list;
    }

    @Override
    public List<KtMetadataField> findAll() {
        return list();
    }

    @Override
    public void delete(KtMetadataField entity) {
        removeById(entity.getId());
    }

    @Override
    public void deleteAll(Iterable<? extends KtMetadataField> entities) {
        for (KtMetadataField entity : entities) {
            removeById(entity.getId());
        }
    }
}
