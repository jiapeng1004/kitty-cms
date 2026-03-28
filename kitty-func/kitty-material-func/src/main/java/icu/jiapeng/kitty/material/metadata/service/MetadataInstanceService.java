package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataInstance;

import java.util.List;

public interface MetadataInstanceService extends IService<KtMetadataInstance> {

    void clearLastFlag(String resourceId, String templateId);

    Integer findMaxVersion(String resourceId, String templateId);

    void saveAll(List<KtMetadataInstance> rows);

    List<KtMetadataInstance> findLast(String resourceId, String templateId);

    List<KtMetadataInstance> findByResourceTemplateVersionUpTo(String resourceId, String templateId, int maxVersionInclusive);
}
