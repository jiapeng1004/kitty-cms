package icu.jiapeng.kitty.material.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplateFieldBinding;

import java.util.List;

public interface MetadataTemplateFieldBindingService extends IService<KtMetadataTemplateFieldBinding> {

    List<KtMetadataTemplateFieldBinding> listByTemplateIdOrderBySort(String templateId);

    boolean replaceBindings(String templateId, List<KtMetadataTemplateFieldBinding> bindings);
}
