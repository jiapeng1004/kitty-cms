package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplate;

import java.util.List;
import java.util.Optional;

public interface MetadataTemplateService extends IService<KtMetadataTemplate> {

    KtMetadataTemplate saveTemplate(KtMetadataTemplate entity);

    List<KtMetadataTemplate> saveAll(Iterable<KtMetadataTemplate> entities);

    Optional<KtMetadataTemplate> findById(String id);

    List<KtMetadataTemplate> findAll();

    void delete(KtMetadataTemplate entity);

    void deleteAll(Iterable<? extends KtMetadataTemplate> entities);

    List<KtMetadataTemplate> listEnabledByCatalogAndType(String catalogId, Integer resourceType);
}
