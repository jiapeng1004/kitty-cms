package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplateFieldBinding;
import icu.jiapeng.kitty.material.metadata.mapper.KtMetadataTemplateFieldBindingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetadataTemplateFieldBindingServiceImpl extends ServiceImpl<KtMetadataTemplateFieldBindingMapper, KtMetadataTemplateFieldBinding> implements MetadataTemplateFieldBindingService {

    @Override
    public List<KtMetadataTemplateFieldBinding> listByTemplateIdOrderBySort(String templateId) {
        return lambdaQuery()
                .eq(KtMetadataTemplateFieldBinding::getTemplateId, templateId)
                .orderByAsc(KtMetadataTemplateFieldBinding::getSortNum)
                .orderByAsc(KtMetadataTemplateFieldBinding::getFieldId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replaceBindings(String templateId, List<KtMetadataTemplateFieldBinding> bindings) {
        remove(lambdaQuery()
                .eq(KtMetadataTemplateFieldBinding::getTemplateId, templateId));
        if (bindings == null || bindings.isEmpty()) {
            return true;
        }
        return saveBatch(bindings);
    }
}
