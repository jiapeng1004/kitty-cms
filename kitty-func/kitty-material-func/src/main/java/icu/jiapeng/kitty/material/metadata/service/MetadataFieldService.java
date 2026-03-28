package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataField;

import java.util.List;
import java.util.Optional;

public interface MetadataFieldService extends IService<KtMetadataField> {

    Optional<KtMetadataField> findById(String id);

    KtMetadataField saveField(KtMetadataField entity);

    List<KtMetadataField> saveAll(Iterable<KtMetadataField> entities);

    List<KtMetadataField> findAll();

    void delete(KtMetadataField entity);

    void deleteAll(Iterable<? extends KtMetadataField> entities);
}
