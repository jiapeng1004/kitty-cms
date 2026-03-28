package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataInstance;
import icu.jiapeng.kitty.material.metadata.mapper.KtMetadataInstanceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetadataInstanceServiceImpl extends ServiceImpl<KtMetadataInstanceMapper, KtMetadataInstance> implements MetadataInstanceService {

    @Override
    public void clearLastFlag(String resourceId, String templateId) {
        lambdaUpdate()
                .eq(KtMetadataInstance::getResourceId, resourceId)
                .eq(KtMetadataInstance::getTemplateId, templateId)
                .eq(KtMetadataInstance::getLastVersion, 1)
                .set(KtMetadataInstance::getLastVersion, 0)
                .update();
    }

    @Override
    public Integer findMaxVersion(String resourceId, String templateId) {
        List<KtMetadataInstance> one = lambdaQuery()
                .select(KtMetadataInstance::getVersion)
                .eq(KtMetadataInstance::getResourceId, resourceId)
                .eq(KtMetadataInstance::getTemplateId, templateId)
                .orderByDesc(KtMetadataInstance::getVersion)
                .last("LIMIT 1")
                .list();
        if (one.isEmpty()) {
            return 0;
        }
        return one.get(0).getVersion();
    }

    @Override
    public void saveAll(List<KtMetadataInstance> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        saveBatch(rows);
    }

    @Override
    public List<KtMetadataInstance> findLast(String resourceId, String templateId) {
        return lambdaQuery()
                .eq(KtMetadataInstance::getResourceId, resourceId)
                .eq(KtMetadataInstance::getTemplateId, templateId)
                .eq(KtMetadataInstance::getLastVersion, 1)
                .orderByAsc(KtMetadataInstance::getFieldId)
                .list();
    }

    @Override
    public List<KtMetadataInstance> findByResourceTemplateVersionUpTo(String resourceId, String templateId, int maxVersionInclusive) {
        return lambdaQuery()
                .eq(KtMetadataInstance::getResourceId, resourceId)
                .eq(KtMetadataInstance::getTemplateId, templateId)
                .le(KtMetadataInstance::getVersion, maxVersionInclusive)
                .orderByAsc(KtMetadataInstance::getVersion)
                .orderByAsc(KtMetadataInstance::getFieldId)
                .list();
    }
}
